name := "sharing-backend"

lazy val root = (project in file("."))
  .enablePlugins(sbtdocker.DockerPlugin)

version := "0.1"

scalaVersion := "2.12.8"

mainClass in assembly := Some("ekb.validol.sharing.backend.Boot")
mainClass in (Compile, run) := Some("ekb.validol.sharing.backend.Boot")

assemblyMergeStrategy in assembly := {
  case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
  case x                                           => (assemblyMergeStrategy in assembly).value(x)
}

dockerfile in docker := {
  new Dockerfile {
    from("anapsix/alpine-java:8u202b08_jdk")
    copy(assembly.value, "/app/assembly.jar")
    copy(file("./start.sh"), "/app/start.sh")
    run("chmod", "+x", "/app/start.sh")
    expose(9999)
    cmd("/app/start.sh")
  }
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "io.spray" %%  "spray-json" % "1.3.5",
  "org.scalactic" %% "scalactic" % "3.0.8",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "org.mockito"   %  "mockito-core" % "1.9.5" % "test"
)