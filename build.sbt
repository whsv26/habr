ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "habr",
    idePackagePrefix := Some("org.whsv26.habr")
  )

libraryDependencies ++= Seq(
  "com.rometools" % "rome" % "1.18.0",
  "org.http4s" %% "http4s-dsl" % "0.23.11",
  "org.http4s" %% "http4s-blaze-client" % "0.23.11",
  "org.jsoup" % "jsoup" % "1.14.3", // parse html
  "org.seleniumhq.selenium" % "selenium-java" % "4.1.4", // render js
  "io.github.bonigarcia" % "webdrivermanager" % "5.1.1",
  "org.slf4j" % "slf4j-nop" % "1.7.36",
  "com.github.cb372" %% "cats-retry" % "3.1.0",
  ("com.github.pureconfig" %% "pureconfig" % "0.17.1").cross(CrossVersion.for3Use2_13),
)
