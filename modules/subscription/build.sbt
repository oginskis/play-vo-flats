import play.sbt.PlayImport.guice

name := """subscription"""

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies ++= Seq("org.mongodb" % "mongo-java-driver" % "3.4.1",
  ws,
  guice,
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % "test",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0" % "test",
  "net.ruippeixotog" %% "scala-scraper" % "2.0.0-RC2"
)