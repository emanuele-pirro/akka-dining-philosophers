name := "akka-dining-philosophers"

version := "1.0"

scalaVersion := "2.12.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.2"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.2" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.5.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"


