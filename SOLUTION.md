# Lab 2: Solution 

This solution uses Java Hadoop MapReduce API to process input files and counting the total references of URLs. I have created a new file named `UrlCount.java` which contains the same boilerplate code as `WordCount1.java`, and it uses regex pattern matching to extract all `href` from the input HTML lines. 
This program reads the input data from HDFS, processes the URLs using mapper and reducer tasks, and writes the results back to HDFS.

We also have a `Makefile` that has commands to compile the java code, download the input files, running the MapReduce job.

The program was executed first on jupyterLab and then on Google Cloud Dataproc cluster.

## Software and Environment

The following software and services are required:

-   Java

-   Apache Hadoop


-   Google Cloud Dataproc
    
-   gcloud sdk (optional, can be done directly from GCP GUI/terminal)
    
The java program requires the Hadoop libraries available through the Hadoop classpath which we take care of in the Makefile.

## Resources Used

-    <a href="https://hadoop.apache.org/docs/r3.3.6/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html#Inputs_and_Outputs">Apache Hadoop documentation</a>
    
-    <a href="https://www.skills.google/focuses/585?parent=catalog">Google Cloud Dataproc tutorial</a>

-    <a href="https://docs.cloud.google.com/managed-spark/docs/concepts/clusters-overview">Google Cloud Dataproc documentation</a>
- <a href="https://docs.cloud.google.com/sdk/docs/cheatsheet">Google Cloud Documentation - For general GCP commands</a>
- <a href="https://chatgpt.com/">ChatGPT - For clearing conceptual doubts regarding the hadoop/mapReduce architecture</a>

    

## Collaboration

I worked independently on this assignment and did not work with anyone else.


## Steps to Run the Solution


### 1. Create a Dataproc Cluster with 2 Workers

```bash
gcloud dataproc clusters create lab2-cluster \
    --region=us-central1 \
    --zone=us-central1-a \
    --master-machine-type=e2-standard-4 \
    --worker-machine-type=e2-standard-4 \
    --num-workers=2 \
    --public-ip-address
```

### 2. SSH into the Master Node

```bash
gcloud compute ssh lab2-cluster-m --zone=us-central1-a
```

### 3. Clone the Repository

Clone the project repository onto the master node:

```bash
git clone https://github.com/tanay13/lab2-url-lister.git
```

Then navigate into the project directory:

```bash
cd lab2-url-lister
```

### 4. Prepare the Input Files

Run the following command to download the input files and place them in the Hadoop Distributed File System (HDFS):

```bash
make prepare
```

### 5. Recompile the JAR

The JAR file should be compatible with the java version on the cluster, so its better to remove the previous `jar` file (if any) and then recompile the program.

```bash
rm -f UrlCount.jar
```

Then compile the program:

```bash
make UrlCount.jar

```

### 6. Run the MapReduce Job

Run the following command to execute the `UrlCount` MapReduce program:

```bash
time hadoop jar UrlCount.jar UrlCount input output
```

Using `time` to measure the execution time


### 7. View the Output

You can view the output from the hadoop `output` directory

```bash
hdfs dfs -cat output/*
```


## Execution Time Comparison

The same input was processed using Dataproc clusters with 2 and 4 worker nodes.

### With 2 worker cluster

![2 worker cluster](./image/2-worker-cluster.jpg)

### With 4 worker cluster

![4 worker cluster](./image/4-worker-cluster.jpg)

For this lab we should focus on the `real` time as it measures the actual elapsed time from when you start the Hadoop job until it finishes.

<b>2 workers </b>
-	Input Split -  2 
-	Time -  43.461 seconds 

<b>4 workers </b>
-	Input Split - 2 
-	Time - 50.250 seconds 

Time taken by both the clusters are almost the same which seems counter-intuitive at first but since our input files are very small compared to the HDFS block size we only get two input splits. So even with 4 worker nodes we end up getting almost the same performance because the remaining 2 worker nodes are useless and are never used in the Mapping stage.

However in this case we see that 4-worker cluster took longer than the 2 worker cluster, this generally happens because  the 4 worker cluster launches more reduce tasks compared to the 2 worker cluster. Since the input dataset was relatively small, the additional task scheduling and coordination overhead outweighed the benefits of having more workers. As a result, the 4 worker cluster took approximately 7 seconds longer than the 2 worker cluster.

This shows that adding more worker nodes does not always make the job faster. For the extra workers to actually help, there needs to be enough work for them to process in parallel.
