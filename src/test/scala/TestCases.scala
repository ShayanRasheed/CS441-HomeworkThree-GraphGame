package com.lsc

import org.scalatest.funsuite.AnyFunSuite
import com.lsc.Main.findConfidenceScore
import com.typesafe.config.ConfigFactory

// Tests
// Scala Tests for confidence score calculations and valuable node distance algorithm
// Tests use files on publicly accessible s3 bucket
class TestCases extends AnyFunSuite {
  private val config = ConfigFactory.load()
  val loader = new GraphLoader(config.getString("Test.originalFilePath"))
  val pLoader = new GraphLoader(config.getString("Test.perturbedFilePath"))

  test("Simple confidence score test") {
    assert(findConfidenceScore(loader, pLoader, 1, returnType = false).toDouble == 0.5)
  }

  test("Confidence score test - two nodes w/ same edges") {
    assert(findConfidenceScore(loader, pLoader, 2, returnType = false).toDouble == 1)
  }

  test("Valuable node distance test - One valuable node in range") {
    loader.setCurNode(7)
    assert(loader.valuableNodeDist().get == 2)
  }

  test("Valuable node distance test - Two valuable nodes in range, closest should be returned") {
    loader.setCurNode(10)
    assert(loader.valuableNodeDist().get == 1)
  }

  test("Valuable node distance test - No valuable nodes in range") {
    loader.setCurNode(14)
    assert(loader.valuableNodeDist().getOrElse(0) == 0)
  }
}
