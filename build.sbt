name := "solutions-site"

version := "1.3-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)     

libraryDependencies += "com.google.code.javaparser" % "javaparser" % "1.0.8"

libraryDependencies += "org.psjava" % "psjava" % "0.1.10"


play.Project.playJavaSettings
