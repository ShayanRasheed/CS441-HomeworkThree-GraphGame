# Police and Thief Graph Game

## Table of Contents
1. [Introduction](#introduction)
2. [Setup Guide](#setup-guide)
3. [Deployment Video](#deployment-video)
4. [Dependencies](#dependencies)
5. [Code Structure and Logic](#code-structure-and-logic)

## Introduction
This program involves creating a game using a graph. There are two players: a policeman and a thief.
The policeman's goal is to catch the thief by moving to the same node as them. The thief's goal
is to locate and move to a "valuable" node on the graph. A player can select their role and submit moves
by sending http requests. The other player's moves will be determined automatically by the program. 

## Setup Guide
1. **Begin by Cloning the Repository:**
   ```
   git clone https://github.com/ShayanRasheed/CS441-HomeworkThree-GraphGame
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
   to create a fat JAR of the project


5. **Run the Application:**
   ```
   sbt run
   ```

## Deployment Video
Here is a [video demonstrating deployment of the program on AWS]() 

## Dependencies

   1. **Logback and SLFL4J**: Used to log messages and warnings during the execution
      of the program. An important tool for debugging and viewing the process of the code

   2. **Typesafe Conguration Library**: Used to define application settings and other values
      for use during program execution

   3. **Google Guava Library**: Used to build the graph in which each player moves

   4. **Akka HTTP**: Framework used to accept and respond to HTTP requests

## Code Structure and Logic

### Loading a Graph:
The program begins by loading a graph provided by NetGameSim in **GraphLoader**. This is done using a
.txt file created by **ngsConverter** which turns the original ngs file into a format 
used by the main program. The file is used to create a google guava graph for use during the game.

### Allowing client to choose a role:
After a graph is loaded, the http server will be initialized. From here, a user can choose to play as either
the policeman or the thief. They must make this choice in order to begin the game and
submit moves. Once they have selected a role, they will be notified of their starting position.

## Client submits their moves:
Now, the client can submit their next move. After every move, the client will be sent a list
of all nodes that they can move to along with each node's confidence score that indicates
how similar that node is to its counterpart on the perturbed graph. They will also be sent
the distance to the nearest valuable node in the graph within a certain range determined by 
the maxDepth parameter in application.config

## AI opponent makes their move:
Once a player has submitted a move, the AI opponent will make their move. The program will
check all neighbors to the current node and select the node that has the highest confidence score.
The client will be notified of the opponent's position after it makes a move.

## Winning/Losing Conditions:
The client will win the game if they are playing as the police and move to the same
node as the thief, or if they are playing as the thief and move to a node that is valuable.
Likewise, they will lose the game if the AI opponent accomplishes either of these
conditions.





