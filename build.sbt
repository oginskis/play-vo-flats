
name := """play-vo-flats"""

version := "5.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.mongodb" % "mongo-java-driver" % "3.4.1",
  "javax.mail" % "mail" % "1.4",
  "junit" % "junit" % "4.12",
  "org.scalaj" % "scalaj-http_2.11" % "2.3.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "net.ruippeixotog" %% "scala-scraper" % "1.2.0"
)

fork in run := false


import AssemblyKeys._
assemblySettings

mainClass in assembly := Some("play.core.server.ProdServerStart")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

mergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
  case "META-INF/services/com.fasterxml.jackson.databind.Module" => MergeStrategy.first
  case x if x.startsWith("org/apache/commons/logging") => MergeStrategy.first
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

