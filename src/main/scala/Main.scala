package com.lsc.Main

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Main {
  private val config = ConfigFactory.load()
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    println("Hello World!")
  }
}
