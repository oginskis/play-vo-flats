import play.sbt.PlayImport.guice

name := """notification"""

libraryDependencies ++= Seq(ws,guice,"javax.mail" % "mail" % "1.4",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % "test",
  "com.github.kirviq" % "dumbster" % "1.7.1"
)

