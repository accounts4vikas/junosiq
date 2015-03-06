package net.juniper.iq.stream

import java.math.BigInteger
import net.juniper.iq.MetricsBeanWithMap
import net.juniper.iq.Properties
import net.juniper.iq.PropertyConstants
import net.juniper.iq.stream.jvision.HeapInfo
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.Durations
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import scala.Tuple2
import StreamingDemo._
import org.apache.spark.streaming.kafka.KafkaUtils
import scala.collection.JavaConversions._
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import com.fasterxml.jackson.databind.ObjectMapper
import net.juniper.iq.stream.jvision.CpuData
import java.io.IOException
import org.apache.log4j.Logger
import net.juniper.iq.MyFunctions
import java.util.HashMap
import net.juniper.iq.Constants
import com.datastax.spark.connector.streaming._

object StreamingDemo {
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
    
    val app = new StreamingDemo(conf)
    app.run()
  }
}

class StreamingDemo private (@transient var conf: SparkConf) extends Serializable with PropertyConstants {

  def run() = {
    val ssc = new StreamingContext(conf, Durations.seconds(Int.int2long((properties.getInt(SPARK_STREAM_BATCH_KEY)))))
    ssc.checkpoint(properties.getString(SPARK_CP_DIR_KEY))
    computeWindows(ssc)
    ssc.awaitTermination()
  }
  
  private def computeWindows(ssc: StreamingContext) = {
    println("Processing Windows...")
    
    val topicMap = scala.collection.immutable.Map(
      properties.getString(KAFKA_TOPIC_KEY) -> Int.unbox(properties.getInt(KAFKA_PARALLEL_KEY))
    )
    val kafkaMsgs = KafkaUtils.createStream(ssc, properties.getString(SPARK_ZOOKEEPER_KEY), 
        properties.getString(KAFKA_CONSUMER_GROUP_KEY), topicMap, StorageLevel.MEMORY_AND_DISK)
    
    val heapInfoFlatMapDS = kafkaMsgs.map(_._2).flatMap(jsonStr => MyFunctions.jsonToHeapInfo(jsonStr)).cache()

    val interval1WindowDS = heapInfoFlatMapDS.window(new Duration(Int.int2long(properties.getInt(SPARK_WINDOW_SIZE1_KEY))), 
        new Duration(Int.int2long(properties.getInt(SPARK_WINDOW_SIZE1_KEY))))
    val interval2WindowDS = heapInfoFlatMapDS.window(new Duration(Int.int2long(properties.getInt(SPARK_WINDOW_SIZE2_KEY))), 
        new Duration(Int.int2long(properties.getInt(SPARK_WINDOW_SIZE2_KEY))))
    val interval3WindowDS = heapInfoFlatMapDS.window(new Duration(Int.int2long(properties.getInt(SPARK_WINDOW_SIZE3_KEY))), 
        new Duration(Int.int2long(properties.getInt(SPARK_WINDOW_SIZE3_KEY))))
    
    processWindow(interval1WindowDS, properties.getString(CASS_CF_INTV1_KEY))
    processWindow(interval2WindowDS, properties.getString(CASS_CF_INTV2_KEY))
    processWindow(interval3WindowDS, properties.getString(CASS_CF_INTV3_KEY))
    
    ssc.start()
  }

  private def processWindow(windowDS: DStream[HeapInfo], tableName: String) {
    val kvMappedDS = windowDS.map(heapInfo => (heapInfo.getName, heapInfo.getUtilization)).cache()
    
    val kvAvgResultPairDS = kvMappedDS.mapValues(value => (value,1))
        .reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2))
        .mapValues(value => (value._1/value._2))
        
    val kvMinReducedDS = kvMappedDS.reduceByKey((x,y) => MyFunctions.minReduceByKeyPair(x,y))
    
    val kvMaxReducedDS = kvMappedDS.reduceByKey((x,y) => MyFunctions.maxReduceByKeyPair(x,y))
    
    val kvMinMaxJoinedDS = kvMinReducedDS.join(kvMaxReducedDS)
    val kvResultsDS = kvMinMaxJoinedDS.join(kvAvgResultPairDS)
    
    val kvResultsMappedDS = kvResultsDS.map (input => MyFunctions.tupleToMetricsBeanWithMap(input)) 

    kvResultsMappedDS.saveToCassandra(properties.getString(CASS_KEYSPACE_KEY), tableName)
  }

}
