ThisBuild / scalaVersion := "2.12.17"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """ml-journey-notify""",
    libraryDependencies ++= Seq(
      guice,
      "com.johnsnowlabs.nlp" %% "spark-nlp" % "4.4.2",
      "org.apache.spark" %% "spark-mllib" % "3.4.0" % "provided",
      "com.typesafe.play" %% "play-slick" % "4.0.0-M4",
      "org.postgresql" % "postgresql" % "42.6.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    )
  )