package com.lsc

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object Main {
  private val config = ConfigFactory.load()
  private val logger = LoggerFactory.getLogger(getClass)

  private val origLoader = new GraphLoader(config.getString("Local.originalFilePath"))
  private val perturbedLoader = new GraphLoader(config.getString("Local.perturbedFilePath"))

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    // Var to hold the version of the game being used
    var version = ""
    var aiVersion = ""

    val route =
      path("police") {
        get {
          if(version.length < 1) {
            version = "police"
            aiVersion = "thief"
            logger.info("Client chose policeman role")

            val startNode = origLoader.chooseStartNode(None)
            if(perturbedLoader.setStartNode(startNode)) {
              val result = outputNextMove()

              result match {
                case Some(result) =>
                  complete (HttpEntity (ContentTypes.`text/html(UTF-8)`, s"You chose to play the role of Policeman\nYour starting node is $startNode\n" + result))
                case None =>
                  complete (HttpEntity (ContentTypes.`text/html(UTF-8)`, "No neighbors found. You've lost the game."))
              }
            }
            else {
              version = ""
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Initial node does not exist on perturbed graph. Please resend http request."))
            }
          }
          else {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "You've already selected a role"))
          }
        }
      } ~
        path("thief") {
          get {
            if(version.length < 1) {
              version = "thief"
              aiVersion = "police"
              logger.info("Client chose thief role")
              val startNode = origLoader.chooseStartNode(None)
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>You chose to play the role of Thief</h1>\n<h1>Your starting node is $startNode"))
            }
            else {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>You've already selected a role</h1>"))
            }
          }
        }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server now online. Please navigate to http://localhost:8080/")

    scala.sys.addShutdownHook {
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
    }
  }

  private def validateMove(): Unit = {

  }

  private def outputNextMove(): Option[List[String]] = {
    val neighbors = origLoader.getNeighbors
    println(neighbors)

    if(neighbors.isEmpty) {
      None
    }
    else {
      val result = neighbors.map(x => findConfidenceScore(x))


      Some(result)
    }
  }

  private def findConfidenceScore(node: Int) : String = {
    val origNeighbors : List[Int] = origLoader.findNeighbors(node)
    val perturbedNeighbors : List[Int] = perturbedLoader.findNeighbors(node)

    val commonNeighbors = origNeighbors.intersect(perturbedNeighbors)
    val confidenceScore = commonNeighbors.size.toDouble / origNeighbors.size

    s"Node $node - Confidence Score: $confidenceScore\n"
  }
}
