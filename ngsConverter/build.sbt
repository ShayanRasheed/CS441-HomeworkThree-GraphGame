ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

name := "ngsConverter"

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
  "io.circe" %% "circe-core" % "0.14.5",
  "io.circe" %% "circe-generic" % "0.14.5",
  "io.circe" %% "circe-yaml" % "0.14.2"
)

// Define your project and its dependencies.
lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= commonDependencies // Adding common dependencies to your project
  )

Compile / mainClass := Some("com.cvt.Main")
