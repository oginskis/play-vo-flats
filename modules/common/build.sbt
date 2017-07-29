import play.sbt.PlayImport.guice

name := """common"""

libraryDependencies ++= Seq("org.mongodb" % "mongo-java-driver" % "3.4.1",guice)