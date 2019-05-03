organization  := "ch.unibas.cs.gravis"

name := """landmarks-clicker"""

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

version := "0.3.0"

scalaVersion  := "2.12.6"

crossScalaVersions := Seq("2.12.6", "2.11.8")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.jcenterRepo

resolvers += Resolver.bintrayRepo("unibas-gravis", "maven")

resolvers += "Statismo (public)" at "http://shapemodelling.cs.unibas.ch/repository/public"

resolvers += Opts.resolver.sonatypeSnapshots

libraryDependencies  ++= Seq(
    "ch.unibas.cs.gravis" %% "scalismo-faces" % "0.10.1"
)

mainClass in assembly := Some("scalismo.faces.apps.LMClicker")

assemblyJarName in assembly := "landmarks-clicker.jar"
