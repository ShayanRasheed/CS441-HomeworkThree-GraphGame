package com.cvt

import NetGraphAlgebraDefs.{Action, NetGraphComponent, NodeObject}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import java.io.{File, FileInputStream, FileNotFoundException, InputStream, ObjectInputStream}
import java.net.URL
import java.io.PrintWriter
import scala.util.{Failure, Success, Try}

object loadGraph {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  def loadFile(path: String) : Unit = {
    val fileName = config.getString(s"$path.filePath")
    logger.info(s"Loading Graph Info from $fileName")

    Try {
      val inputStream: InputStream = if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
        val url = new URL(fileName)
        url.openStream()
      } else {
        new FileInputStream(new File(fileName))
      }

      val ois = new ObjectInputStream(inputStream)
      val ng = ois.readObject.asInstanceOf[List[NetGraphComponent]]

      inputStream.close()
      ois.close()

      ng
    } match {
      case Success(lstOfNetComponents) =>
        logger.info("Returning Graph Object")
        lstOfNetComponents.foreach(x => println(x))

        val vertices = lstOfNetComponents.collect {
          case node: NodeObject => (node.id.toLong, node.valuableData)
        }

        val edges = lstOfNetComponents.collect {
          case action: Action => (action.fromNode.id.toLong, action.toNode.id.toLong)
        }

        val outputPath = config.getString(s"$path.outputFilePath")
        val writer = new PrintWriter(outputPath)
        logger.info(s"Outputting graph info to $outputPath")

        vertices.foreach { vertex =>
          writer.println(s"${vertex._1} ${vertex._2}")
        }
        edges.foreach { edge =>
          writer.println(s"${edge._1} ${edge._2}")
        }

        writer.close()
      case Failure(e: FileNotFoundException) =>
        logger.error(s"File not found: $fileName", e)
      case Failure(e) =>
        logger.error("An error occurred while loading the graph", e)
    }
  }

}
