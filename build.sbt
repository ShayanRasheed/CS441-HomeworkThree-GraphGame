import sbt.Keys.libraryDependencies

// Define the version of your project.
ThisBuild / version := "0.1.0"

// Define the Scala version to be used.
ThisBuild / scalaVersion := "2.12.15"

// Define the project name.
name := "GraphWalks"

// Define library versions. Adjust the version numbers according to your needs.
val scalaTestVersion = "3.2.15"
val typeSafeConfigVersion = "1.4.2"
val logbackVersion = "1.4.0"
val sfl4sVersion = "2.0.5"
val graphVizVersion = "0.18.1"

// Define common dependencies shared across your project.
lazy val commonDependencies = Seq(
  "com.typesafe" % "config" % typeSafeConfigVersion, // Typesafe Config Library
  "ch.qos.logback" % "logback-classic" % logbackVersion, // Logback Classic Logger
  "org.slf4j" % "slf4j-api" % sfl4sVersion, // SLF4J API Module
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test, // ScalaTest for testing
  "org.yaml" % "snakeyaml" % "2.0"
)

// Define your project and its dependencies.
lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= commonDependencies // Adding common dependencies to your project
  )

// Define Scala Compiler options.
scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs
  //"--explain-types", // Explain type errors in more detail
  "-feature" // Emit warning and location for usages of features that should be imported explicitly
)

// Define JVM options for running your project.
compileOrder := CompileOrder.JavaThenScala
test / fork := true
run / fork := true
run / javaOptions ++= Seq(
  "-Xms512M", // Initial JVM heap size
  "-Xmx2G", // Maximum JVM heap size
  "-XX:+UseG1GC" // Use G1 Garbage Collector
)

//scalacOptions ++= Seq("-target:jvm-1.8")

exportJars := true

//assemblyMergeStrategy in assembly := {
//  case PathList("META-INF", _*) => MergeStrategy.discard
//  case _                        => MergeStrategy.first
//}

// Define the main class. Replace with the actual main class of your application.
Compile / mainClass := Some("com.lsc.Main")
run / mainClass := Some("com.lsc.Main")
