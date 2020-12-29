import sbt._
val aws                  = "com.amazonaws"

object Dependencies {
  lazy val buildDependencies = Seq(
    aws             % "aws-lambda-java-core"              % "1.2.0"
  )

  lazy val testDependencies = Seq(
    "org.mockito"   % "mockito-core" % "2.23.0" % "test",
    "org.scalatest" %% "scalatest"   % "3.0.4"  % "test"
  )
}
