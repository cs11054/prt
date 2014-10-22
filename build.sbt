name := "mods"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "com.typesafe.play" %% "play-slick" % "0.5.0.8",
  "com.h2database" % "h2" % "1.3.166",
   "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
  cache
)     

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

play.Project.playScalaSettings
