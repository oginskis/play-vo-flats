import sbt.Keys.{fork, libraryDependencies}
import sbtassembly.Plugin.AssemblyKeys._

name := """flats"""

lazy val commonSettings = Seq(
  version := "12.29",
  scalaVersion := "2.12.3"
)

lazy val commonAssemblySettings = Seq(
  mainClass in assembly := Some("play.core.server.ProdServerStart"),
  fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
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
    case x if x.contains("application.conf") => MergeStrategy.concat
    case x if x.contains("routes") => MergeStrategy.first
    case x if x.contains("Routes") => MergeStrategy.first
    case x if x.contains("Module") => MergeStrategy.first
    case x =>
      val oldStrategy = (mergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val flats = (project in file("."))
  .settings(
    commonSettings,
    resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns),
    libraryDependencies ++= Seq(
      guice,
      ws,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
      "javax.mail" % "mail" % "1.4",
      "junit" % "junit" % "4.12",
      "org.scalaj" % "scalaj-http_2.11" % "2.3.0",
      "net.ruippeixotog" %% "scala-scraper" % "2.0.0-RC2",
      "com.typesafe.akka" % "akka-testkit_2.12" % "2.5.3" % Test
    ),
    fork in run := false,
    assemblySettings,
    commonAssemblySettings
  )
  .enablePlugins(PlayScala)
  .dependsOn(find,search,subscription,api)
  .aggregate(find,search,subscription,api)

lazy val api = (project in file("modules/api"))
  .settings(commonSettings)

lazy val common = (project in file("modules/common"))
  .settings(commonSettings,
    fork in run := false,assemblySettings,commonAssemblySettings)
  .enablePlugins(PlayScala)

lazy val find = (project in file("modules/find"))
  .settings(commonSettings,
    fork in run := false,assemblySettings,commonAssemblySettings)
  .enablePlugins(PlayScala)
  .dependsOn(api,common)
  .aggregate(api,common)

lazy val search = (project in file("modules/search"))
  .settings(commonSettings,
    fork in run := false,assemblySettings,commonAssemblySettings)
  .enablePlugins(PlayScala)
  .dependsOn(api)
  .aggregate(api)

lazy val subscription = (project in file("modules/subscription"))
  .settings(commonSettings,
    fork in run := false,assemblySettings,commonAssemblySettings)
  .enablePlugins(PlayScala)
  .dependsOn(api,common)
  .aggregate(api,common)


