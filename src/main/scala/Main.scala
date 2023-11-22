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
  private val AILoader = new GraphLoader(config.getString("Local.originalFilePath"))

  private val perturbedLoader = new GraphLoader(config.getString("Local.perturbedFilePath"))

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    // Var to hold the version of the game being used
    var playingAsPolice : Option[Boolean] = None
    var gameInProgress = false

    val route =
      path("police") {
        get {
          playingAsPolice match {
            case None =>
              playingAsPolice = Some(true)
              logger.info ("Client chose policeman role")

              val startNode = origLoader.chooseStartNode (None)
              if (perturbedLoader.setStartNode (startNode) ) {
                val result = outputNextMove (origLoader)

                result match {
                  case Some (result) =>
                    gameInProgress = true
                    complete(s"You chose to play the role of Policeman\nYour starting node is $startNode\n" + result.mkString (", ") )
                  case None =>
                    complete("No neighbors found. You've lost the game.")
                }
              }
              else {
                playingAsPolice = None
                complete (s"Initial node does not exist on perturbed graph. Please resend http request.")
              }
            case Some(bool) =>
              complete ("You've already selected a role")
          }
        }
      } ~
        path("thief") {
          get {
            playingAsPolice match {
              case None =>
                playingAsPolice = Some(false)
                logger.info ("Client chose thief role")

                val startNode = origLoader.chooseStartNode(None)
                if (perturbedLoader.setStartNode(startNode)) {
                  val result = outputNextMove(origLoader)

                  result match {
                    case Some(result) =>
                      gameInProgress = true
                      complete(s"You chose to play the role of Thief\nYour starting node is $startNode\n" + result.mkString(", "))
                    case None =>
                      complete("No neighbors found. You've lost the game.")
                  }
                }
                else {
                  playingAsPolice = None
                  complete(s"Initial node does not exist on perturbed graph. Please resend http request.")
                }
              case Some(bool) =>
                complete ("You've already selected a role")
            }
          }
        } ~
        path("submitMove") {
          get {
            if(gameInProgress) {
              parameters('number.as[Int]) { number =>
                val result = validateMove(origLoader, number, playingAsPolice.get)
                println(result)

                result match {
                  case 0 => complete(s"Invalid move. Please submit a valid node ${origLoader.getNeighbors}")
                  case -1 =>
                    gameInProgress = false
                    complete("You've moved to a node that is not in the perturbed graph. You've lost the game")
                  case 2 =>
                    gameInProgress = false
                    complete("You've moved to a valuable node. You've won the game!")
                  case 1 =>
                    complete(outputNextMove(origLoader))
                }
              }
            }
            else {
              complete("Cannot submit move. The game is not in progress.")
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

  private def validateMove(loader: GraphLoader, node: Int, isPolice: Boolean): Int = {
    val checkOne = loader.getNeighbors.contains(node)
    val checkTwo = perturbedLoader.findNeighbors(loader.getCurNode).contains(node)

    if(checkOne && !checkTwo) {
      logger.info("Move that was selected was in original graph, but not in perturbed")
      -1
    }
    else if(checkOne && checkTwo) {
      loader.setCurNode(node)

      if(!isPolice && loader.checkValuable(node)) {
        logger.info("The thief has reached a valuable node")
        2
      }
      // TODO: Add check if police is on same node as thief
      else {
        logger.info("Move successful. The game will continue.")
        1
      }
    }
    else {
      logger.info("Invalid move.")
      0
    }
  }

  private def outputNextMove(loader: GraphLoader): Option[List[String]] = {
    val neighbors = loader.getNeighbors

    if(neighbors.isEmpty) {
      None
    }
    else {
      val maxDepth = config.getInt("App.maxDepth")

      val nextNodes = List("Here are the next nodes you can move to:\n")
      val confidenceScores = neighbors.map(x => findConfidenceScore(loader, x))

      val result = nextNodes ::: confidenceScores

      val valNodeDist = loader.valuableNodeDist()
      println(valNodeDist)

      val distOutput: String = valNodeDist match {
        case Some(dist) => s"There is a valuable node $dist moves away!"
        case None => s"No valuable nodes detected within $maxDepth moves"
      }

      // TODO: Print other player's location

      Some(result :+ distOutput)
    }
  }

  private def findConfidenceScore(loader: GraphLoader, node: Int) : String = {
    val origNeighbors : List[Int] = loader.findNeighbors(node)
    val perturbedNeighbors : List[Int] = perturbedLoader.findNeighbors(node)

    val commonNeighbors = origNeighbors.intersect(perturbedNeighbors)
    val confidenceScore = commonNeighbors.size.toDouble / origNeighbors.size

    s"Node $node - Confidence Score: $confidenceScore\n"
  }
}