import play.sbt.PlayImport.guice

name := """common"""

libraryDependencies ++= Seq("org.mongodb" % "mongo-java-driver" % "3.4.1",
  guice,
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0")