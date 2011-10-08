name := "HoNStats"

version := "0.3"

organization := "com.oldsch00l"

scalaVersion := "2.9.1"

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
	"-dontshrink",
	"-dontpreverify",
	"-dontnote",
	keepMain("libHoN.HoNStats"),
	keepAllScala
)
