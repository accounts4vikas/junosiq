package net.juniper.iq

//remove if not needed
import scala.collection.JavaConversions._

trait PropertyConstants {

  final val SPARK_APP_NAME_KEY = "stream.appname"

  final val SPARK_SERVER_KEY = "stream.spark.server"

  final val SPARK_CP_DIR_KEY = "stream.checkpoint.dir"

  final val SPARK_BATCH_APP_NAME_KEY = "batch.appname"

  final val SPARK_STREAM_BATCH_KEY = "stream.batch_interval"

  final val SPARK_ZOOKEEPER_KEY = "stream.zkhosts"

  final val KAFKA_TOPIC_KEY = "stream.kafka_topic"

  final val KAFKA_PARALLEL_KEY = "stream.kafka_parallelization"

  final val KAFKA_CONSUMER_GROUP_KEY = "stream.kafka_consumer_group_id"  

  //final val SPARK_CASS_CONN = "spark.cassandra.connection.host"

  //final val SPARK_CASS_URL_KEY = "stream.cassandra.url"

  final val CASS_KEYSPACE_KEY = "stream.cassandra.keyspace"

  final val CASS_SEC_CF_KEY = "stream.cassandra.table"

  final val SPARK_WINDOW_SIZE1_KEY = "stream.window.size.interval1"

  final val SPARK_WINDOW_SIZE2_KEY = "stream.window.size.interval2"

  final val SPARK_WINDOW_SIZE3_KEY = "stream.window.size.interval3"

  final val CASS_CF_INTV1_KEY = "stream.cassandra.table.interval1"

  final val CASS_CF_INTV2_KEY = "stream.cassandra.table.interval2"

  final val CASS_CF_INTV3_KEY = "stream.cassandra.table.interval3"

  final val SPARK_BATCH_NEXT_BATCH_FREQUENCY_WINDOW_KEY = "batch.nextbatch.frequency.window"

  final val SPARK_BATCH_SMALL_INT_TBL_KEY = "batch.table.small.interval"

  final val SPARK_BATCH_BIG_INT_TBL_KEY = "batch.table.big.interval"
}
