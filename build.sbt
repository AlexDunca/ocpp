import ScalaxbKeys._

val dispatchV = "0.11.2"
val json4sV = "3.2.10"
val sprayV = "1.3.1"
val specs2V = "2.4.15"

val time = "com.thenewmotion" %% "time" % "2.8"
val json4sNative = "org.json4s" %% "json4s-native" % json4sV
val json4sExt = "org.json4s" %% "json4s-ext" % json4sV
val hookup = "io.backchat.hookup" %% "hookup" % "0.4.0"
// TODO switch to newer logging
val logging = "com.typesafe" %% "scalalogging-slf4j" % "1.0.1"
val dispatch = "net.databinder.dispatch" %% "dispatch-core" % dispatchV
val scalax = "ua.t3hnar.scalax" %% "scalax" % "1.7"
val sprayHttp = "io.spray" %% "spray-http" % sprayV
val sprayHttpX = "io.spray" %% "spray-httpx" % sprayV
val sprayExt = "com.thenewmotion" %% "spray-ext" % "0.1.2"
val specs2 = "org.specs2" %% "specs2" % specs2V % "test"


val basicSettings = Seq(scalaVersion := "2.10.4",
                        // -Ywarn-unused-import is not supported in 2.10
                        scalacOptions := Seq(
                              "-encoding", "UTF-8",
                              "-unchecked",
                              "-deprecation",
                              "-feature",
                              "-Xlog-reflective-calls"),
                        libraryDependencies += specs2)

def module(name: String) = Project(name, file(name))
                             .enablePlugins(OssLibPlugin)
                             .settings(basicSettings)
                             .settings(aether.Aether.aetherPublishSettings)

def scalaxbModule(name: String, packageNameForGeneratedCode: String) =
  module(name)
   .settings(libraryDependencies += dispatch)
   .settings(scalaxbSettings: _*)
   .settings(
     sourceGenerators in Compile += (scalaxb in Compile).taskValue,
     dispatchVersion in (Compile, scalaxb) := dispatchV,
     packageName in (Compile, scalaxb)     := packageNameForGeneratedCode)


val messages = module("ocpp-messages").settings(libraryDependencies += time)
val json = module("ocpp-json").dependsOn(messages)
             .settings(libraryDependencies ++= List(hookup, json4sNative, json4sExt, logging, time))
val ocpp12Soap = scalaxbModule("ocpp-12", "com.thenewmotion.ocpp.v12")
val ocpp15Soap = scalaxbModule("ocpp-15", "com.thenewmotion.ocpp.v15")
val ocppSoap = module("ocpp-soap")
                 .dependsOn(messages, ocpp12Soap, ocpp15Soap)
                 .settings(libraryDependencies ++= List(logging, scalax))
val ocppSpray = module("ocpp-spray")
                  .dependsOn(ocppSoap)
                  .settings(libraryDependencies ++= List(sprayHttp, sprayHttpX, sprayExt))

publish := {}
