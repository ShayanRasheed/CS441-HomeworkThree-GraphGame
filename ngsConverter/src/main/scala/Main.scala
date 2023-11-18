package com.cvt.Main

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import com.cvt.loadGraph.loadFile

object Main {
  private val config = ConfigFactory.load()
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    loadFile("original")
    loadFile("perturbed")
  }
}