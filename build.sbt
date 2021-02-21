name := "transit-service"
scalaVersion := "2.13.4"
assemblyJarName in assembly := "transit-service.jar"

libraryDependencies ++= Seq(
  "com.amazonaws"     % "aws-lambda-java-events" % "2.2.6",
  "com.amazonaws"     % "aws-lambda-java-core"   % "1.2.0",
  "com.amazonaws"     % "aws-java-sdk-s3"        % "1.11.945",
  "commons-io"        % "commons-io"             % "2.8.0",
  "io.spray"          %% "spray-json"            % "1.3.6",
  "org.scalatest"     %% "scalatest"             % "3.2.2" % "test",
  "org.mockito"       % "mockito-core"           % "3.6.0" % "test",
  "org.scalatestplus" %% "mockito-3-4"           % "3.2.3.0" % "test"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings" // Fail the compilation if there are any warnings.
)
