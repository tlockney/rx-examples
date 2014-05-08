val rxVersion = "0.18.1"
val hystrixVersion = "1.4.0-RC3"

name := "rx-examples"

organization := "net.lockney"

version := "0.1.0-SNAPSHOT"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

javaHome := Some(file("/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/"))

libraryDependencies ++= Seq(
  "com.netflix.rxjava" % "rxjava-core" % rxVersion,
  "com.netflix.rxjava" % "rxjava-scala" % rxVersion,
  "com.netflix.rxnetty" % "rx-netty" % "0.3.3",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.netflix.hystrix" % "hystrix-core" % hystrixVersion,
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test"
)

