name := "bulletf"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

scalacOptions += "-feature"

javaOptions ++= Seq("-verbose:gc", "-Dfile.encoding=UTF-8")

initialCommands in console += "import com.github.whelmaze.bulletf._"

seq(lwjglSettings: _*)

compile in Compile <<= compile in Compile mapR {
  case Inc(inc: Incomplete) =>
    Sound.play("sound/cut01.wav")
    throw inc
  case Value(v) =>
    Sound.play("sound/pickup01.wav")
    v
}
