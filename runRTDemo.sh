spark-submit  --files ./log4j.properties \
              --driver-java-options -Dconfig.file=./runtimeProps.conf \
              --class net.juniper.iq.stream.StreamingDemo \
              --master local[2] \
              --name "JSparkStreamDemoNew" \
              ./target/scala-2.10/junosiq.jar \
             $@
