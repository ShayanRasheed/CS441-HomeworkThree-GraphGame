# Police and Thief Graph Game

## Table of Contents
1. [Introduction](#introduction)
2. [Setup Guide](#setup-guide)
3. [Deployment Video](#deployment-video)
4. [Dependencies](#dependencies)
5. [Code Structure and Logic](#code-structure-and-logic)

## Introduction

## Setup Guide
1. **Begin by Cloning the Repository:**
   ```
   git clone https://github.com/ShayanRasheed/CS441-HomeworkTwo-GraphWalks.git
   ```


2. **Navigate to Project Directory:**
   ```
   cd CS441-HomeworkThree-GraphGame
   ```
3. If you have an ngs file, first go to **ngsConverter** and
   set the local path to the ngs file in application.conf. Then, compile and run
   the ngsConverter to turn the ngs file into a .txt file which is usable as input by
   the main program. You can also skip this step by setting the isOnCloud parameter to true in the
   application.conf of GraphGame to use the .txt files that are already stored on an Amazon s3 bucket


4. **Compile:**

    Be sure to check application.conf prior to compiling to ensure all the
    configuration parameters are set to the values you'd like them to be.
   
    ```
   sbt clean compile
    ```
   Alternitavely, you can use
    
    ```
   sbt clean assembly
    ```
   to create a fat JAR of the project to deploy on AWS


5. **Run the Application:**
   ```
   sbt run
   ```
   You can also submit the job via spark-submit

## Deployment Video
Here is a [video demonstrating deployment of the program on AWS]() 

## Dependencies

   1. **Logback and SLFL4J**: Used to log messages and warnings during the execution
      of the program. An important tool for debugging and viewing the process of the code

   2. **Typesafe Conguration Library**: Used to define application settings and other values
      for use during program execution

   3. 

## Code Structure and Logic

### Loading a Graph:
The program begins by loading a graph provided by NetGameSim in **GraphLoader**. This is done using a
.txt file created by **ngsConverter** which turns the original ngs file into a format 
used by the main program. The file is used to create a graphX graph object that consists of 
two RDDs for the vertices and edges.





