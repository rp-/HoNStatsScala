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
}
