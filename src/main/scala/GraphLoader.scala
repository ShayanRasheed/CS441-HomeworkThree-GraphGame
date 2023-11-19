package com.lsc

import org.slf4j.LoggerFactory

import java.io._
import java.net.URL
import scala.io.Source
import scala.util.{Failure, Success, Try}
import com.google.common.graph.{GraphBuilder, MutableGraph}

import scala.collection.mutable.ListBuffer

class GraphLoader {
  private val logger = LoggerFactory.getLogger(getClass)
  private val valuableNodes: ListBuffer[Int] = ListBuffer.empty

  // LOAD GRAPH
  // Creates graph object from text file produced by ngsConverter
  // Can load graph from local file as well as s3 bucket
  def loadGraph(fileName: String): Option[MutableGraph[String]] = {
    logger.info(s"Loading the NetGraph from $fileName")

    Try {
      val source = if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
        val url = new URL(fileName)
        Source.fromURL(url)
      } else {
        Source.fromFile(fileName)
      }

      val graph: MutableGraph[String] = GraphBuilder.undirected().build()

      source.getLines.foreach { line =>
        val parts = line.split(" ")
        parts.length match {
          case 2 => graph.putEdge(parts(0), parts(1))
          case 3 => graph.addNode(parts(1))
            if(parts(2).equals("true")) {
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
        Some(graph)
      case Failure(e: FileNotFoundException) =>
        logger.error(s"File not found: $fileName", e)
        None
      case Failure(e) =>
        logger.error("An error occurred while loading the graph", e)
        None
    }
  }

}