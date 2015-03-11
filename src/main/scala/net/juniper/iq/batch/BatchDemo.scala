package net.juniper.iq.batch

import java.io.Serializable
import java.math.BigInteger
import java.util.Date
import java.util.HashMap
import java.util.Map
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
import net.juniper.iq.MetricsBeanDB
import org.joda.time.DateTime

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

case class MetricsBeanTemp(eventKey: String, eventTime: Long, minValue: Long, maxValue: Long, avgValue: Long)

class BatchDemo private (@transient var conf: SparkConf) extends Serializable with PropertyConstants {

  private def run() {
    val sc = new SparkContext(conf)
    compute(sc)
    sc.stop()
  }

  private def compute(sc: SparkContext) {

    val metricsFromCassSmallIntvRDD = sc.cassandraTable[MetricsBeanDB](properties.getString(CASS_KEYSPACE_KEY), properties.getString(SPARK_BATCH_SMALL_INT_TBL_KEY))
    val metricBeanSmallIntvRDD = metricsFromCassSmallIntvRDD.map(beanWithMap => new MetricsBeanTemp(beanWithMap.eventKey, beanWithMap.eventTime.getMillis, 
            beanWithMap.minValue, beanWithMap.maxValue, beanWithMap.avgValue)).cache()
            
    if(metricBeanSmallIntvRDD.count() == 0) {
        println("**********************************************");
        println("No data in small interval table, exiting...");
        println("**********************************************");     
        System.exit(1);
    } 

    val sqlContext = new SQLContext(sc)
    import sqlContext.createSchemaRDD
    
    metricBeanSmallIntvRDD.registerTempTable("metrics")

    val metricsFromCassBigIntvRDD = sc.cassandraTable[MetricsBeanDB](properties.getString(CASS_KEYSPACE_KEY), properties.getString(SPARK_BATCH_BIG_INT_TBL_KEY))
    
    if (metricsFromCassBigIntvRDD.count() == 0) {
      println("**********************************************")
      println("Inserting for first time")
      println("**********************************************")
  
      val row = sqlContext.sql("SELECT eventTime FROM metrics ORDER BY eventTime DESC LIMIT 1").collect().head
      val lastTime = new DateTime(row.getLong(0))
      println("Last entry time = " + lastTime)

      val aggMBWithMapRDD = sqlContext.sql("SELECT eventKey, MIN(minValue), MAX(maxValue), SUM(avgValue), COUNT(*) FROM metrics GROUP BY eventKey")
          .map(row => MyFunctions.rowToMetricsBean(row, lastTime)) 
          
      aggMBWithMapRDD.saveToCassandra(properties.getString(CASS_KEYSPACE_KEY), properties.getString(SPARK_BATCH_BIG_INT_TBL_KEY))
      
    } else {
      println("**********************************************")
      println("Inserting for next interval = " + properties.getInt(SPARK_BATCH_NEXT_BATCH_FREQUENCY_WINDOW_KEY)/60 + " minutes")
      println("**********************************************")
      
      var lastInsertTime = metricsFromCassBigIntvRDD.first().eventTime
      var lastInsertTimePlusWindow = lastInsertTime.plusSeconds(properties.getInt(SPARK_BATCH_NEXT_BATCH_FREQUENCY_WINDOW_KEY))
      
      while (lastInsertTimePlusWindow.isBefore(org.joda.time.DateTime.now())) {
        val sqlWhereSubStr = new StringBuffer()
        sqlWhereSubStr.append(" WHERE eventTime > ")
        sqlWhereSubStr.append(lastInsertTime.getMillis)
        sqlWhereSubStr.append(" AND eventTime <= ")
        sqlWhereSubStr.append(lastInsertTimePlusWindow.getMillis)
        
        val countCheckSQL = "SELECT COUNT(*) FROM metrics" + sqlWhereSubStr.toString
        val getFilteredDataSQL = "SELECT eventKey, MIN(minValue), MAX(maxValue), SUM(avgValue), COUNT(*) FROM metrics" + sqlWhereSubStr.toString + " GROUP BY eventKey"
        
        val row = sqlContext.sql(countCheckSQL).collect().head
        
        val count = row.getLong((0))
        if (count > 0) {
          println("Found " + count + " records between " + lastInsertTime + " and " + lastInsertTimePlusWindow + ". Processing...")
          val lastTimeInner = new DateTime(lastInsertTimePlusWindow.getMillis)
          val aggFilteredMBWithMapRDD = sqlContext.sql(getFilteredDataSQL)
              .map(row => MyFunctions.rowToMetricsBean(row, lastTimeInner)) 
              
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
