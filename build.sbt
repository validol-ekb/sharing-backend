name := "sharing-backend"

version := "0.1"

scalaVersion := "2.12.8"

mainClass in (Compile, run) := Some("ekb.validol.sharing.backend.Boot")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "io.spray" %%  "spray-json" % "1.3.5",
  "org.scalactic" %% "scalactic" % "3.0.8",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "org.mockito"   %  "mockito-core" % "1.9.5" % "test"
)