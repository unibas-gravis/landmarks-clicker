organization := "ch.unibas.cs.gravis"

name := """landmarks-clicker"""

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

version := "0.3.0"

scalaVersion := "2.13.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.jcenterRepo

resolvers += Opts.resolver.sonatypeSnapshots

libraryDependencies ++= Seq(
  "ch.unibas.cs.gravis" %% "scalismo-faces" % "0.90.0"
)

mainClass in assembly := Some("scalismo.faces.apps.LMClicker")

assemblyJarName in assembly := "landmarks-clicker.jar"
