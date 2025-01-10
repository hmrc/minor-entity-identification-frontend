import sbt.*

object AppDependencies {
  val bootstrapPlayVersion: String = "9.7.0"
  val mongoPlayVersion: String = "2.3.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % "11.9.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoPlayVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapPlayVersion  % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"  % mongoPlayVersion      % Test,
    "org.jsoup"               % "jsoup"                     % "1.18.1"              % Test,
    "com.vladsch.flexmark"    % "flexmark-all"              % "0.64.8"              % Test,
    "org.mockito"             %% "mockito-scala-scalatest"  % "1.17.37"             % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "7.0.1"               % Test,
    "org.scalatestplus"       %% "mockito-3-4"              % "3.2.10.0"            % Test
  )

}
