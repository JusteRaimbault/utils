scalaVersion := "2.9.2"

scalaSource in Compile <<= baseDirectory(_ / "src")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
                      "-encoding", "us-ascii")

libraryDependencies +=
  "org.nlogo" % "NetLogo" % "5.0.1" from
    "http://ccl.northwestern.edu/netlogo/5.0.1/NetLogo.jar"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6"


artifactName := { (_, _, _) => "convol.jar" }

packageOptions := Seq(
  Package.ManifestAttributes(
    ("Extension-Name", "convol"),
    ("Class-Manager", "ConvolutionExtension"),
    ("NetLogo-Extension-API-Version", "5.0")))


    packageBin in Compile := {
      val jar = (packageBin in Compile).value
      val classpath = (dependencyClasspath in Runtime).value
      val base = baseDirectory.value
      val s = streams.value
      IO.copyFile(jar, base / "convol.jar")
      def pack200(name: String) {
        Process("/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/pack200 " +
                "--modification-time=latest --effort=9 --strip-debug " +
                "--no-keep-file-order --unknown-attribute=strip " +
                name + ".pack.gz " + name).!!
      }
      pack200("convol.jar")
      val libraryJarPaths =
        classpath.files.filter{path =>
          path.getName.endsWith(".jar") &&
          !path.getName.startsWith("scala-library")}
      for(path <- libraryJarPaths) {
        IO.copyFile(path, base / path.getName)
        pack200(path.getName)
      }
      if(Process("git diff --quiet --exit-code HEAD").! == 0) {
        // copy everything thing we need for distribution in
        // a temporary "nw" directory, which we will then zip
        // before deleting it.
        IO.createDirectory(base / "convol")
        val zipExtras =
          (libraryJarPaths.map(_.getName) :+ "convol.jar")
            .filterNot(_ contains "NetLogo")
            .flatMap{ jar => Seq(jar, jar + ".pack.gz") }
        for(extra <- zipExtras)
          IO.copyFile(base / extra, base / "convol" / extra)
        for (dir <- Seq("alternate-netlogolite", "demo"))
          IO.copyDirectory(base / dir, base / "convol" / dir)
        Process("zip -r convol.zip convol").!!
        IO.delete(base / "convol")
      }
      else {
        s.log.warn("working tree not clean; no zip archive made")
        IO.delete(base / "convol.zip")
      }
      jar
    }

cleanFiles <++= baseDirectory { base =>
  Seq(base / "convol.jar",
      base / "convol.jar.pack.gz",
      base / "convol.zip") }
