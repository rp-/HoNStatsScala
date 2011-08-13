name := "HoNStats"

version := "0.2"

organization := "com.oldsch00l"

scalaVersion := "2.9.0"

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
	"-dontshrink",
	"-dontpreverify",
	"-dontnote",
	keepMain("libHoN.HoNStats"),
	keepAllScala
)
