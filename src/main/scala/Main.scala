package com.lsc

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Main {
  private val config = ConfigFactory.load()
  private val logger = LoggerFactory.getLogger(getClass)

  private val origLoader = new GraphLoader()
  private val perturbedLoader = new GraphLoader()

  def main(args: Array[String]): Unit = {
    val originalGraph = origLoader.loadGraph(config.getString("Local.originalFilePath"))
    val perturbedGraph = perturbedLoader.loadGraph(config.getString("Local.perturbedFilePath"))
  }
}
