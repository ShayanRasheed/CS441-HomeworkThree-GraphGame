# Random Parallel Graph Walks

## Table of Contents
1. [Introduction](#introduction)
2. [Setup Guide](#setup-guide)
3. [Deployment Video](#deployment-video)
4. [Dependencies](#dependencies)
5. [Code Structure and Logic](#code-structure-and-logic)

## Introduction
In this project, random walks are performed on a graph structure that 
is taken in as input through a file. The walks are meant to simulate Man-in-the-middle
attacks, where each walk represents a potential attack on a system with a graph-like structure.

The goal of the walks is to search for nodes with valuable data that the attacker wants
to retrieve. However, it is unclear which nodes of the graph genuinely contain valuable data
or if they are simply honeypots designed to detect attacks.

The program performs a large number of these walks in parallel and outputs the results
containing statistics on how many were successful and how many failed. It also identifies
which specific nodes were found and which were searched along each walk.

## Setup Guide
1. **Begin by Cloning the Repository:**
   ```
   git clone https://github.com/ShayanRasheed/CS441-HomeworkTwo-GraphWalks.git
   ```


2. **Navigate to Project Directory:**
   ```
   cd CS441-HomeworkTwo-GraphWalks
   ```
3. If you have an ngs file, first go to **ngsConverter** and
   set the local path to the ngs file in application.conf. Then, compile and run
   the ngsConverter to turn the ngs file into a .txt file which is usable as input by
   the main program. You can also skip this step by setting the isOnCloud parameter to true in the
   application.conf of GraphWalks to use the .txt files that are already stored on an Amazon s3 bucket


4. **Compile:**

    Be sure to check application.conf prior to compiling to ensure all the
    configuration parameters are set to the values you'd like them to be.

    Also check Main.scala to set the spark setup depending
    on if you are running locally or via AWS/spark-submit.
   
    ```
   sbt clean compile
    ```
   Alternitavely, you can use
    
    ```
   sbt clean assembly
    ```
   to create a fat JAR of the project to deploy on AWS or run via spark-submit


5. **Run the Application:**
   ```
   sbt run
   ```
   You can also submit the job via spark-submit

## Deployment Video
Here is a [video demonstrating deployment of the program on AWS](https://youtu.be/CcFsaMTlvTk) 

## Dependencies

   1. **Logback and SLFL4J**: Used to log messages and warnings during the execution
      of the program. An important tool for debugging and viewing the process of the code

   2. **Typesafe Conguration Library**: Used to define application settings and other values
      for use during program execution

   3. **Apache Spark**: Framework used to parallelize operations on the graph efficiently

   4. **GraphX**: Provides utilities to analyze graph data

## Code Structure and Logic

### Loading a Graph:
The program begins by loading a graph provided by NetGameSim in **GraphLoader**. This is done using a
.txt file created by **ngsConverter** which turns the original ngs file into a format 
used by the main program. The file is used to create a graphX graph object that consists of 
two RDDs for the vertices and edges.

### Performing multiple random walks:
After the graph is loaded, the program will begin to perfrom random walks on the graph.
The starting node is chosen at random and will also choose a neighboring node to search at random.
This will continue until either a valuable node match is found or until the length of the walk has
exceeded the number of searches defined in application.conf

For each node that the walk goes through in the search, it will check the similarity between
that node and all the valuable nodes from the original graph. This is done using
**SimRank** which is a class that computes a similarity value between two nodes 
by how many neighbors they share

If a walk fails to find a valuable node, the walk will begin again while avoiding any nodes it
has already searched previously.

### Outputting Results:
Finally, the results of each walk will be printed, including details about
which nodes were searched on each walk, which nodes were returned by
each walk as a match, how many attacks succeeded and failed, and the ratios
of successful and failed attacks to total attacks

If the program is executed locally, these details are also output as a yaml file to the path
set in application.conf





