import uk.gov.hmrc.DefaultBuildSettings
import scoverage.ScoverageKeys

val appName = "minor-entity-identification-frontend"

lazy val scoverageSettings = {
  val exclusionList: List[String] = List(
    "<empty>",
    ".*Routes.*",
    ".*Reverse.*",
    "app.*",
    "prod.*",
    "config.*",
    "com.kenshoo.play.metrics.*",
    "testOnlyDoNotUseInAppConf.*",
    "uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.api.*",
    "uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.frontend.*",
    "uk.gov.hmrc.minorentityidentificationfrontend.testonly.*",
    "uk.gov.hmrc.minorentityidentificationfrontend.views.html.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := exclusionList.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(scoverageSettings)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Assets / pipelineStages := Seq(gzip),
    PlayKeys.playDefaultPort := 9725,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s"
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
