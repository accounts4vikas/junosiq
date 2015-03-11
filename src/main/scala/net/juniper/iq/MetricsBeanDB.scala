package net.juniper.iq

import org.joda.time.DateTime

case class MetricsBeanDB (eventKey: String, eventDay: String, eventTime: DateTime, minValue: Long, maxValue: Long, avgValue: Long)