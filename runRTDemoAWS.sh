/home/ubuntu/spark-1.2.1-bin-hadoop2.4/bin/spark-submit \
            --files ./log4j.properties \
            --driver-java-options -Dconfig.file=./runtimePropsAWS.conf \
            --class net.juniper.iq.stream.StreamingDemo \
            --master spark://ip-10-10-0-10:7077 \
            --name "JSparkStreamDemoNew" \
            --total-executor-cores 3 \
            --executor-memory 4g \
            ./junosiq.jar \
            $@