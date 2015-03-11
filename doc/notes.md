##############################################

IMP for creating Spark Cluster on EC2 to create password less ssh
# On master: 
Step 1: log in to ec2 using your .pem key
Step 2: create two files 1. "touch /home/user-name/.ssh/id_rsa" 2. "touch /home/user-name/.ssh/id_rsa.pub"
Step 3: "cat /home/user-name/.ssh/authorized_keys >> /home/user-name/.ssh/id_rsa.pub"
Step 4: "cat /your-private-key.pem >> /home/user-name/.ssh/id_rsa" 

id_rsa is the private key in which you need to copy the contents of .pem file. You can do this by bringing your .pem file on master node using WinSCP or other tools.
Also change the file permissions of both id_rsa , id_rsa.pub to "chmod 600 id_rsa" "chmod 600 id_rsa.pub"

# On workers:
# copy ~/.ssh/id_rsa.pub from your master to the worker, then use:
$ cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
$ chmod 644 ~/.ssh/authorized_keys



##############################################
IMP Add following line to spark-env.sh to ensure that 
you do not run out of disk space on workers node

SPARK_WORKER_OPTS="-Dspark.worker.cleanup.enabled=true -Dspark.worker.cleanup.interval=600 -Dspark.worker.cleanup.appDataTtl=3600"

##############################################
