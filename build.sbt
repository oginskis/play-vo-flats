
name := """play-vo-flats"""

lazy val commonSettings = Seq(
  version := "12.4",
  scalaVersion := "2.12.2"
)

lazy val voFlatsApi = (project in file("voFlatsApi"))
  .settings(commonSettings)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .dependsOn(voFlatsApi)
  .aggregate(voFlatsApi)

resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  guice,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "org.mongodb" % "mongo-java-driver" % "3.4.1",
  "javax.mail" % "mail" % "1.4",
  "junit" % "junit" % "4.12",
  "org.scalaj" % "scalaj-http_2.11" % "2.3.0",
  "net.ruippeixotog" %% "scala-scraper" % "2.0.0-RC2",
  "com.typesafe.akka" % "akka-testkit_2.12" % "2.5.3" % Test
)

fork in run := false


import sbtassembly.Plugin.AssemblyKeys._

assemblySettings

mainClass in assembly := Some("play.core.server.ProdServerStart")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

mergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
  case "META-INF/services/com.fasterxml.jackson.databind.Module" => MergeStrategy.first
  case x if x.startsWith("META-INF/javamail") => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case x if x.startsWith("com/sun/mail/") => MergeStrategy.last
  case x if x.startsWith("javax/mail/" +
    "") => MergeStrategy.last
  case x if x.startsWith("org/apache/commons/logging") => MergeStrategy.first
  case x if x.startsWith("play/api/libs/ws/package") => MergeStrategy.first
  case x if x.contains("play/reference-overrides.conf") => MergeStrategy.concat
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

