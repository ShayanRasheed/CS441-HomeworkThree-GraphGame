package com.lsc

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import com.lsc.GraphLoader.loadGraph

object Main {
  private val config = ConfigFactory.load()
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    loadGraph(config.getString("Local.originalFilePath"))
  }
}
