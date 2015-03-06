package net.juniper.iq

import org.apache.log4j.Logger
import org.apache.commons.configuration.Configuration
import org.apache.commons.configuration.PropertiesConfiguration
import Properties._
//remove if not needed
import scala.collection.JavaConversions._

object Properties {

  private val LOGGER = Logger.getLogger(classOf[Properties])

  private var singleton: Properties = _

  private var propertyPath: String = _

  def get(path: String): Properties = {
    if (singleton == null) singleton = new Properties(path)
    singleton
  }
}

class Properties private (path: String) {

  private var config: Configuration = _

  try {
    this.config = new PropertiesConfiguration(this.getClass.getResource(path))
  } catch {
    case ex: Exception => {
      LOGGER.fatal("Could not load configuration", ex)
      LOGGER.trace(null, ex)
    }
  }

  def getString(key: String): String = config.getString(key)

  def getInt(key: String): java.lang.Integer = config.getInt(key)
}
