import play.sbt.PlayImport.guice

name := """notification"""

libraryDependencies ++= Seq(ws,guice,"javax.mail" % "mail" % "1.4")