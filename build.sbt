import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / scalacOptions += "-Wconf:msg=Flag.*repeatedly:s"

val wErrorScalacOption: String = "-Werror"

lazy val microservice = Project("income-tax-vc-fs-and-stub", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-unchecked",
      wErrorScalacOption)
  )
  .settings(CodeCoverageSettings.settings: _*)
  .settings(PlayKeys.playDefaultPort := 65535)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(scalacOptions += wErrorScalacOption)
  .settings(libraryDependencies ++= AppDependencies.it)
