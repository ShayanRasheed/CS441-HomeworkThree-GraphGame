package com.lsc

import org.slf4j.LoggerFactory

import java.io._
import java.net.URL
import scala.io.Source
import scala.util.{Failure, Success, Try}
import com.google.common.graph.{Graph, GraphBuilder, Graphs, MutableGraph}
import com.typesafe.config.ConfigFactory

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable.ListBuffer
import scala.util.Random

class GraphLoader(fileName: String) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()
  private val valuableNodes: ListBuffer[Int] = ListBuffer.empty
  private val graph: MutableGraph[String] = GraphBuilder.undirected().build()

  private var curNode : Int = _

  logger.info(s"Loading the NetGraph from $fileName")

  Try {
    val source = if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
      val url = new URL(fileName)
      Source.fromURL(url)
    } else {
      Source.fromFile(fileName)
    }

    source.getLines.foreach { line =>
      val parts = line.split(" ")
      parts.length match {
        case 2 => graph.putEdge(parts(0), parts(1))
        case 3 => graph.addNode(parts(1))
          if (parts(2).equals("true")) {
            valuableNodes += parts(1).toInt
          }
      }
    }

    source.close()
    println(graph.toString)
    println(valuableNodes)

    graph
  } match {
    case Success(graph) =>
      logger.info("Successfully created graph")
    case Failure(e: FileNotFoundException) =>
      logger.error(s"File not found: $fileName", e)
    case Failure(e) =>
      logger.error("An error occurred while loading the graph", e)
  }

  def chooseStartNode(chosenNode: Option[Int]) : Int = {
    chosenNode match {
      case Some(node) =>
        val nodes = graph.nodes().toArray()
        val remainingNodes = nodes.filter(_ != node)
        val randomIndex = Random.nextInt(remainingNodes.length)
        val chosenNode = remainingNodes(randomIndex)
        println(chosenNode)
        curNode = chosenNode.toString.toInt
        chosenNode.toString.toInt
      case None =>
        val nodes = graph.nodes().toArray()
        val randomIndex = Random.nextInt(nodes.length)
        val chosenNode = nodes(randomIndex)
        println(chosenNode)
        curNode = chosenNode.toString.toInt
        chosenNode.toString.toInt
    }
  }

  def setStartNode(chosenNode: Int) : Boolean = {
    curNode = chosenNode
    graph.nodes().contains(chosenNode.toString)
  }

  def getNeighbors: List[Int] = {
    val neighbors = graph.adjacentNodes(curNode.toString)
    neighbors.map(_.toInt).toList
  }

  def findNeighbors(node: Int) : List[Int] = {
    val neighbors = graph.adjacentNodes(node.toString)
    neighbors.map(_.toInt).toList
  }

  def valuableNodeDist() : Option[Int] = {
    val maxDepth = config.getInt("App.maxDepth")
    val inputs = List.range(1, maxDepth + 1)
    val outputs: List[List[Int]] = inputs.map(x => allNeighbors(x))
    println(outputs)
    outputs.indexWhere(list => list.exists(node => valuableNodes.contains(node))) match {
      case -1 => None // No matching list found
      case index => Some(index + 1)
    }
  }

  private def allNeighbors(maxDepth: Int): List[Int] = {
    def findNodes(node: Int, currentDepth: Int, maxDepth: Int): List[Int] = {
      if (currentDepth == maxDepth) {
        List(node) // At the specified depth, return the current node
      } else {
        val neighbors = graph.adjacentNodes(node.toString)
        neighbors.flatMap(neighbor => findNodes(neighbor.toInt, currentDepth + 1, maxDepth)).toList
      }
    }

    findNodes(curNode, 0, maxDepth).distinct
  }

}