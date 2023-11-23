package com.lsc

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContextExecutor

object Main {
  private val config = ConfigFactory.load()
  private val logger = LoggerFactory.getLogger(getClass)

  private val origLoader = new GraphLoader(config.getString("Local.originalFilePath"))
  private val AILoader = new GraphLoader(config.getString("Local.originalFilePath"))

  private val perturbedLoader = new GraphLoader(config.getString("Local.perturbedFilePath"))

  private var playingAsPolice : Option[Boolean] = None

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    var gameInProgress = false

    val route =
      path("police") {
        // Client calls this route if they wish to play as the policeman
        get {
          playingAsPolice match {
            case None =>
              playingAsPolice = Some(true)
              logger.info ("Client chose policeman role")

              // pick starting nodes for each player
              val startNode = origLoader.chooseStartNode(None)
              val opponentNode = AILoader.chooseStartNode(Some(startNode))

              if (perturbedLoader.setStartNode (startNode) ) {
                val result = outputNextMove (origLoader)

                result match {
                  case Some (result) =>
                    logger.info(s"Player starting at node $startNode, AI player starting at node $opponentNode")
                    gameInProgress = true
                    complete(s"You chose to play the role of Policeman\nYour starting node is $startNode\n" + result.mkString (", ") + s"\nThe thief will start on Node $opponentNode")
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
          // client calls this route if they wish to play as the thief
          get {
            playingAsPolice match {
              case None =>
                playingAsPolice = Some(false)
                logger.info ("Client chose thief role")

                val startNode = origLoader.chooseStartNode(None)
                val opponentNode = AILoader.chooseStartNode(Some(startNode))

                if (perturbedLoader.setStartNode(startNode)) {
                  val result = outputNextMove(origLoader)

                  result match {
                    case Some(result) =>
                      logger.info(s"Player starting at node $startNode, AI player starting at node $opponentNode")
                      gameInProgress = true
                      complete(s"You chose to play the role of Thief\nYour starting node is $startNode\n" + result.mkString(", ") + s"\nThe policeman will start on Node $opponentNode")
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
          // client uses this route to submit their next move
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
                  case 3 =>
                    gameInProgress = false
                    complete("You've caught the thief. You've won the game!")
                  case 1 =>
                    val opponentResult = chooseNextMove()
                    val opponentOutput: String = opponentResult match {
                      case 0 =>
                        gameInProgress = false
                        "\nYour opponent is on a node with no neighbors. You've won the game!"
                      case -1 =>
                        gameInProgress = false
                        "\nYour opponent made a move that does not exist on the perturbed graph. You've on the game!"
                      case 2 =>
                        gameInProgress = false
                        "\nThe thief has found a valuable node. You've lost the game."
                      case 3 =>
                        gameInProgress = false
                        "\nYou've been caught by the policeman. You've lost the game"
                      case 1 =>
                        s"\nYour opponent has moved to node ${AILoader.getCurNode}"
                    }
                    complete(outputNextMove(origLoader) + opponentOutput)
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

  // validateMove
  // takes in a node id as input and ensures that the node selected is valid
  // also checks victory conditions and returns a different integer based on the result
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
      else if(isPolice && (origLoader.getCurNode == AILoader.getCurNode)) {
        logger.info("The policeman has caught the thief")
        3
      }
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

  // chooseNextMove
  // picks the next node for the AI player
  // returns a value depending on win/loss conditions similarly to validateMove
  private def chooseNextMove() : Int = {
    logger.info("Choosing next node for opponent player...")
    val neighbors = AILoader.getNeighbors

    if(neighbors.isEmpty) {
      0
    }
    else {
      val confidenceScores = neighbors.map(x => findConfidenceScore(AILoader, perturbedLoader, x, returnType = false)).map(_.toDouble)
      val zippedNodes = neighbors.zip(confidenceScores)
      val bestNode = zippedNodes.maxBy(_._2)
      validateMove(AILoader, bestNode._1, !playingAsPolice.get)
    }
  }

  // OutputNextMove
  // Creates a list of strings containing information about each node that the player can move to next
  // Also includes info on how far the nearest valuable node is
  private def outputNextMove(loader: GraphLoader): Option[List[String]] = {
    val neighbors = loader.getNeighbors

    if(neighbors.isEmpty) {
      None
    }
    else {
      val maxDepth = config.getInt("App.maxDepth")

      val nextNodes = List("Here are the next nodes you can move to:\n")
      val confidenceScores = neighbors.map(x => findConfidenceScore(loader, perturbedLoader, x, returnType = true))

      val result = nextNodes ::: confidenceScores

      val valNodeDist = loader.valuableNodeDist()
      println(valNodeDist)

      val distOutput: String = valNodeDist match {
        case Some(dist) => s"There is a valuable node $dist moves away!"
        case None => s"No valuable nodes detected within $maxDepth moves"
      }

      Some(result :+ distOutput)
    }
  }

  // findConfidenceScore
  // Calculates the confidence score for each of a node's neighbors
  // This function compares the node's neighbors in the original and perturbed graph
  // and returns the similarity as a double
  def findConfidenceScore(loader: GraphLoader, pLoader: GraphLoader, node: Int, returnType: Boolean) : String = {
    val origNeighbors : List[Int] = loader.findNeighbors(node)
    val perturbedNeighbors : List[Int] = pLoader.findNeighbors(node)

    val commonNeighbors = origNeighbors.intersect(perturbedNeighbors)
    val confidenceScore = commonNeighbors.size.toDouble / origNeighbors.size

    if(returnType) {
      s"Node $node - Confidence Score: $confidenceScore\n"
    }
    else {
      confidenceScore.toString
    }
  }
}