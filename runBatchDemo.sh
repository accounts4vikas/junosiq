spark-submit  --files ./log4j.properties \
              --driver-java-options -Dconfig.file=./runtimeProps.conf \
              --class net.juniper.iq.batch.BatchDemo \
              --master local[2] \
              --name "JSparkBatchDemoNew" \
             ./target/scala-2.10/junosiq.jar \
             $@