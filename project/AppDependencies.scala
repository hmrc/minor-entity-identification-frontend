import sbt._

object AppDependencies {
  val bootstrapPlayVersion: String = "8.4.0"
  val mongoPlayVersion: String = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % "8.5.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoPlayVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapPlayVersion  % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"  % mongoPlayVersion      % Test,
    "org.jsoup"               % "jsoup"                     % "1.13.1"              % Test,
    "com.vladsch.flexmark"    % "flexmark-all"              % "0.62.2"              % "test, it",
    "org.mockito"             %% "mockito-scala-scalatest"  % "1.17.30"             % Test,
    "com.github.tomakehurst"  % "wiremock-jre8-standalone"  % "3.0.1"               % "it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "7.0.0"               % "test, it",
    "org.scalatestplus"       %% "mockito-3-4"              % "3.2.10.0"            % Test
  )
}
