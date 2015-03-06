package net.juniper.iq.batch

import java.io.Serializable
import java.math.BigInteger
import java.util.Date
import java.util.HashMap
import java.util.Map
import net.juniper.iq.MetricsBeanWithMap
import net.juniper.iq.Properties
import net.juniper.iq.PropertyConstants
import org.apache.spark.SparkConf
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.datastax.spark.connector._
import BatchDemo._
import scala.collection.JavaConversions._
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.SchemaRDD
import org.apache.spark.sql.catalyst.expressions.Row
import net.juniper.iq.Constants
import net.juniper.iq.MyFunctions

object BatchDemo {

  private val SPARK_CASS_CONN = "spark.cassandra.connection.host"
  private val SPARK_CASS_URL_KEY = "stream.cassandra.url"
  
  private var properties: Properties = _
  
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val runtimeConf = ConfigFactory.load()
    val propfilePath = runtimeConf.getString("props.file.path")
    println("props.file.path = " + propfilePath)
    properties = Properties.get(propfilePath)
    
    conf.set(SPARK_CASS_CONN, properties.getString(SPARK_CASS_URL_KEY))
    //conf.set(SPARK_CASS_CONN, "localhost")
  
    val app = new BatchDemo(conf)
    app.run()
  }
}

case class MetricsBean(key: String, time: Long, minValue: Long, maxValue: Long, avgValue: Long)

class BatchDemo private (@transient var conf: SparkConf) extends Serializable with PropertyConstants {

  private def run() {
    val sc = new SparkContext(conf)
    compute(sc)
    sc.stop()
  }

  private def compute(sc: SparkContext) {

    val metricsFromCassSmallIntvRDD = sc.cassandraTable[MetricsBeanWithMap](properties.getString(CASS_KEYSPACE_KEY), properties.getString(SPARK_BATCH_SMALL_INT_TBL_KEY))
    
    val metricBeanSmallIntvRDD = metricsFromCassSmallIntvRDD.map(beanWithMap => new MetricsBean(beanWithMap.getKey, beanWithMap.getTime.getTime, 
            beanWithMap.getAggValues.get(Constants.MIN).longValue(), 
            beanWithMap.getAggValues.get(Constants.MAX).longValue(), 
            beanWithMap.getAggValues.get(Constants.AVG).longValue())).cache()
    

    if(metricBeanSmallIntvRDD.count() == 0) {
        println("**********************************************");
        println("No data in small interval table, exiting...");
        println("**********************************************");     
        System.exit(1);
    } 

    val sqlContext = new SQLContext(sc)
    import sqlContext.createSchemaRDD
    
    metricBeanSmallIntvRDD.registerTempTable("metrics")

    val metricsFromCassBigIntvRDD = sc.cassandraTable[MetricsBeanWithMap](properties.getString(CASS_KEYSPACE_KEY), properties.getString(SPARK_BATCH_BIG_INT_TBL_KEY))
    
    if (metricsFromCassBigIntvRDD.count() == 0) {
      println("**********************************************")
      println("Inserting for first time")
      println("**********************************************")
  
      val row = sqlContext.sql("SELECT time FROM metrics ORDER BY time DESC LIMIT 1").collect().head
      val lastTime = new Date(row.getLong(0))
      println("Last entry toime = " + lastTime)

      val aggMBWithMapRDD = sqlContext.sql("SELECT key, MIN(minValue), MAX(maxValue), SUM(avgValue), COUNT(*) FROM metrics GROUP BY key")
          .map(row => MyFunctions.rowToMetricsBeanWithMap(row, lastTime)) 
              
      
      aggMBWithMapRDD.saveToCassandra(properties.getString(CASS_KEYSPACE_KEY), properties.getString(SPARK_BATCH_BIG_INT_TBL_KEY))
      
    } else {
      println("**********************************************")
      println("Inserting for next interval = " + properties.getInt(SPARK_BATCH_NEXT_BATCH_FREQUENCY_WINDOW_KEY)/60 + " minutes")
      println("**********************************************")
      
      var lastInsertTime = new org.joda.time.DateTime(metricsFromCassBigIntvRDD.first().getTime)
      var lastInsertTimePlusWindow = lastInsertTime.plusSeconds(properties.getInt(SPARK_BATCH_NEXT_BATCH_FREQUENCY_WINDOW_KEY))
      
      while (lastInsertTimePlusWindow.isBefore(org.joda.time.DateTime.now())) {
        val sqlWhereSubStr = new StringBuffer()
        sqlWhereSubStr.append(" WHERE time > ")
        sqlWhereSubStr.append(lastInsertTime.getMillis)
        sqlWhereSubStr.append(" AND time <= ")
        sqlWhereSubStr.append(lastInsertTimePlusWindow.getMillis)
        
        val countCheckSQL = "SELECT COUNT(*) FROM metrics" + sqlWhereSubStr.toString
        val getFilteredDataSQL = "SELECT key, MIN(minValue), MAX(maxValue), SUM(avgValue), COUNT(*) FROM metrics" + sqlWhereSubStr.toString + " GROUP BY key"
        
        val row = sqlContext.sql(countCheckSQL).collect().head
        
        val count = row.getLong((0))
        if (count > 0) {
          println("Found " + count + " records between " + lastInsertTime + " and " + lastInsertTimePlusWindow + ". Processing...")
          val lastTimeInner = new Date(lastInsertTimePlusWindow.getMillis)
          
          val aggFilteredMBWithMapRDD = sqlContext.sql(getFilteredDataSQL)
              .map(row => MyFunctions.rowToMetricsBeanWithMap(row, lastTimeInner)) 
          
          aggFilteredMBWithMapRDD.saveToCassandra(properties.getString(CASS_KEYSPACE_KEY),properties.getString(SPARK_BATCH_BIG_INT_TBL_KEY))
          
          println("Inserted new aggregate for records between " + lastInsertTime + " and " + lastInsertTimePlusWindow + ".")
            
          val tempTime = lastInsertTimePlusWindow
          lastInsertTime = lastInsertTimePlusWindow
          lastInsertTimePlusWindow = tempTime.plusMinutes(5)
        } else {
          println("No entry found between " + lastInsertTime + " and " + lastInsertTimePlusWindow + ". Moving to next interval...")
          val tempTime = lastInsertTimePlusWindow
          lastInsertTime = lastInsertTimePlusWindow
          lastInsertTimePlusWindow = tempTime.plusMinutes(5)
        }
      }
      println("No more new records to be entered...")      
    
    }
    
  }    
}
