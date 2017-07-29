import play.sbt.PlayImport.guice

name := """subscription"""

libraryDependencies ++= Seq("org.mongodb" % "mongo-java-driver" % "3.4.1",ws,guice)