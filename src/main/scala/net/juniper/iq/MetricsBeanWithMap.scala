package net.juniper.iq

import java.io.Serializable
import java.math.BigInteger
import java.util.Date
import java.util.Map
//remove if not needed
import scala.collection.JavaConversions._

class MetricsBeanWithMap extends Serializable {

  var key: String = _

  var time: Date = _

  var aggValues: Map[String, BigInteger] = _

  def this(key: String, time: Date, aggValues: Map[String, BigInteger]) {
    this()
    this.key = key
    this.time = time
    this.aggValues = aggValues
  }

  def getKey(): String = key

  def setKey(key: String) {
    this.key = key
  }

  def getTime(): Date = time

  def setTime(time: Date) {
    this.time = time
  }

  def getAggValues(): Map[String, BigInteger] = aggValues

  def setAggValues(aggValues: Map[String, BigInteger]) {
    this.aggValues = aggValues
  }

  override def toString(): String = {
    val aggValuesStr = new StringBuffer()
    aggValuesStr.append(" [min= " + aggValues.get("min"))
    aggValuesStr.append(", max= " + aggValues.get("max"))
    aggValuesStr.append(", avg= " + aggValues.get("avg") + "]")
    "MetricsBeanWithMap [key=" + key + ", time=" + time + 
      ", aggValues=" + 
      aggValuesStr.toString + 
      "]"
  }

}
