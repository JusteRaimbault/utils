enablePlugins(org.nlogo.build.NetLogoExtension)

scalaVersion := "2.11.7"
//scalaVersion := "2.12.2"

scalaSource in Compile := { baseDirectory.value  / "src" }

javaSource in Compile  := { baseDirectory.value / "src" }

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings",
                        "-encoding", "us-ascii")

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-encoding", "us-ascii")

name := "osm"

netLogoVersion      := "6.0.2"

netLogoClassManager := "OSM"

netLogoExtName      := "osm"

netLogoZipSources   := false

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// https://github.com/plasmap/geow
// alternative : https://github.com/angelcervera/osm4scala
libraryDependencies += "io.plasmap" %% "geow" % "0.3.11-SNAPSHOT"


