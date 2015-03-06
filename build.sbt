name := "junosiq"

organization := "Juniper Networks"

version := "0.0.1"

scalaVersion := "2.10.4"

resolvers ++= Seq("cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0.M5b" % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-core" % "1.2.1" % "provided" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-streaming" % "1.2.1" % "provided" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-streaming-kafka" % "1.2.1",
  "org.apache.spark" %% "spark-sql" % "1.2.1" % "provided" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-mllib" % "1.2.1" % "provided" withSources() withJavadoc(),
  "org.apache.hadoop" % "hadoop-client" % "2.5.0-cdh5.3.1" % "provided" withJavadoc(),
  "org.apache.hadoop" % "hadoop-common" % "2.5.0-cdh5.3.1" % "provided" withJavadoc(),
  "com.github.scopt" %% "scopt" % "3.2.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.1",
  "org.apache.httpcomponents" % "httpclient" % "4.1.3" % "provided",
  "org.apache.httpcomponents" % "httpcore" % "4.1.3" % "provided",
  "log4j" % "log4j" % "1.2.17",
  "com.typesafe" % "config" % "1.2.1",
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.2.0-alpha2"
)

mainClass in (Compile,packageBin) := Some("net.juniper.iq.stream.KafkaJsonProducer")

assemblyJarName in assembly := "junosiq.jar"

assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false)
  
//run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile,packageBin),runner in (Compile, run))

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}