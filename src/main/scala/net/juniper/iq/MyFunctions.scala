package net.juniper.iq

import net.juniper.iq.stream.jvision.HeapInfo
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import net.juniper.iq.stream.jvision.CpuData
import org.apache.log4j.Logger
import java.math.BigInteger
import java.util.HashMap
import org.apache.spark.sql.catalyst.expressions.Row
import java.util.Date
import org.joda.time.DateTime

object MyFunctions extends Serializable {

  def jsonToHeapInfo(jsonInput: String): java.util.List[HeapInfo] = {
      val mapper = new ObjectMapper()
      val cpuData = mapper.readValue(jsonInput, classOf[CpuData])
      cpuData.getValue.getHeapInfo
  }

  def minReduceByKeyPair(x: Long, y: Long): Long = {
    if (x < y) {
          return x 
        } else { 
          return y
        }
  }

  def maxReduceByKeyPair(x: Long, y: Long): Long = {
    if (x < y) {
          return y 
        } else { 
          return x
        }
  }

  def tupleToMetricsBean(input: Tuple2[String, Tuple2[Tuple2[Long, Long], Long]]): MetricsBeanDB = {
    val currentTime = new DateTime(System.currentTimeMillis())
    val day = currentTime.toString("yyyy-MM-dd")
    val metricsBean = new MetricsBeanDB(input._1, day, currentTime, input._2._1._1, input._2._1._2, input._2._2)
    metricsBean
  }
  
  def rowToMetricsBean(row: Row, lastTimeInner: DateTime): MetricsBeanDB = {
    val day = lastTimeInner.toString("yyyy-MM-dd")
    val metricsBean = new MetricsBeanDB(row.getString(0), day, lastTimeInner, row.getLong(1), row.getLong(2), row.getLong(3) / row.getLong(4))
    metricsBean
  } 
  
  /* 
   * This is original anonymous functions which were used inside BatchDemo 
   * and were replaced by function above rowToMetricsBeanWithMap.
   * Keeping them here just in case we need to refer them during any issue 
   */
   /* 
  val aggMBWithMapRDD = sqlContext.sql("SELECT key, MIN(minValue), MAX(maxValue), SUM(avgValue), COUNT(*) FROM metrics GROUP BY key")
      .map(new Function[Row, MetricsBeanWithMap]() {
      def apply(row: Row): MetricsBeanWithMap = {
        var bean = new MetricsBeanWithMap()
        bean.setKey(row.getString(0))
        bean.setTime(lastTime)
        var aggValues = new HashMap[String, BigInteger]()
        aggValues.put(Constants.MIN, BigInteger.valueOf(row.getLong(1)))
        aggValues.put(Constants.MAX, BigInteger.valueOf(row.getLong(2)))
        aggValues.put(Constants.AVG, BigInteger.valueOf(row.getLong(3) / row.getLong(4)))
        bean.setAggValues(aggValues)
        return bean
      }
    })

    val aggMBWithMapRDD = sqlContext.sql("SELECT key, MIN(minValue), MAX(maxValue), SUM(avgValue), COUNT(*) FROM metrics GROUP BY key")
        .map { row => 
          var bean = new MetricsBeanWithMap()
          bean.setKey(row.getString(0))
          bean.setTime(lastTime)
          var aggValues = new HashMap[String, BigInteger]()
          aggValues.put(Constants.MIN, BigInteger.valueOf(row.getLong(1)))
          aggValues.put(Constants.MAX, BigInteger.valueOf(row.getLong(2)))
          aggValues.put(Constants.AVG, BigInteger.valueOf(row.getLong(3) / row.getLong(4)))
          bean.setAggValues(aggValues)
        }
  def rowToMetricsBeanWithMap(row: Row, lastTimeInner: Date): MetricsBeanWithMap = {
    val metricsBean = new MetricsBeanWithMap()
    metricsBean.setKey(row.getString(0))
    metricsBean.setTime(lastTimeInner)
    val aggValues = new HashMap[String, BigInteger]()
    aggValues.put(Constants.MIN, BigInteger.valueOf(row.getLong(1)))
    aggValues.put(Constants.MAX, BigInteger.valueOf(row.getLong(2)))
    aggValues.put(Constants.AVG, BigInteger.valueOf(row.getLong(3) / row.getLong(4)))
    metricsBean.setAggValues(aggValues) 
    metricsBean
  }
   def tupleToMetricsBeanWithMap(input: Tuple2[String, Tuple2[Tuple2[Long, Long], Long]]): MetricsBeanWithMap = {
    val metricsBean = new MetricsBeanWithMap()
    metricsBean.setKey(input._1)
    metricsBean.setTime(new java.util.Date(System.currentTimeMillis()))
    val aggValues = new HashMap[String, BigInteger]()
    aggValues.put(Constants.MIN, BigInteger.valueOf(input._2._1._1))
    aggValues.put(Constants.MAX, BigInteger.valueOf(input._2._1._2))
    aggValues.put(Constants.AVG, BigInteger.valueOf(input._2._2))
    metricsBean.setAggValues(aggValues)
    metricsBean
  }
*/
}