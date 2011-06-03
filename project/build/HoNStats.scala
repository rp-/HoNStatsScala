import sbt._

class HoNStats(info: ProjectInfo) extends DefaultProject(info)
  with ProguardProject with Exec {
  
  //project name
  override val artifactID = "HoNStats"

  //program entry point
  override def mainClass: Option[String] = Some("libHoN.HoNStats")

  //proguard
  override def proguardOptions = List(
	"-dontshrink -dontoptimize -dontobfuscate -dontpreverify -dontnote " +
    "-ignorewarnings",
    proguardKeepAllScala
  )

  override def proguardInJars =
    Path.fromFile(scalaLibraryJar) +++ super.proguardInJars

  lazy val install = task {
	val source = outputPath / (artifactID + "-" + projectVersion.value + ".min.jar")
	val target : Path = Path.fromFile( new java.io.File("/usr/local/bin/HoNStats.jar") )
	sbt.FileUtilities.copyFile( source, target, new ConsoleLogger() )
	None } dependsOn(proguard)
}
