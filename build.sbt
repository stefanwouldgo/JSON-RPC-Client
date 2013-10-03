name := "JSON-RPC-Client"

organization := "com.sagesex"

version := "0.0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.2" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "com.typesafe.play" %% "play-json" % "2.2.0"
)

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository"

// initialCommands := "import com.sagesex.jsonrpcclient._"

