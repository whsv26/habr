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
  "net.sourceforge.htmlunit" % "htmlunit" % "2.61.0", // render js
  "com.github.cb372" %% "cats-retry" % "3.1.0",
)
