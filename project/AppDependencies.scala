import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.1.0",
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "7.1.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "3.23.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.71.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "7.1.0" % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % "0.71.0" % Test,
    "org.jsoup" % "jsoup" % "1.13.1" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % "test, it",
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.55" % Test,
    "com.github.tomakehurst" % "wiremock-jre8-standalone" % "2.31.0" % "it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test, it",
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test
  )
}
