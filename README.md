# JunosIQ Spark Demo

This is a project for demonstrating and testing Kafka, Spark and Spark Streaming 
using JVision log messages. This project consume cpu_mem json messages
from Kafka using Spark Streaming and then create minimum, maximum & 
average for HeapInfo for sources supplied in input data (e.g. Kernel, 
LAN buffer etc.). For each Spark Stream batch duration, calculated results 
are finally inserted into Cassandra database., which can be
used to visualize that data through some UI application. Bigger interval
results (e.g. 5 min, 1 hr.) are calculated using Spark Batch.  


# Setting up locally on your machine

Clone the project and download on your machine
```
git clone git@github.com:accounts4vikas/junosiq.git
```

Compile the code using sbt:

```
% sbt assembly
```

Create tables in your Cassandra database using createtables.cql

Setup properties files to use the correct URLs and tables for your deployment 


# Running the demo

To run Streaming Demo, you can use runRTDemo<XYZ>.sh scripts.
To run Batch Demo, you can use runBatchDemo<XYZ>.sh scripts.

 
Note: You can use KafkaJsonProducer from JunosIQStream project to create a 
stream of test Json messages for testing purpose.
