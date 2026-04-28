import sbt.*

object AppDependencies {
  val bootstrapPlayVersion: String = "10.5.0"
  val mongoPlayVersion: String = "2.11.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % "12.25.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoPlayVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"  % mongoPlayVersion,
    "org.jsoup"               % "jsoup"                     % "1.22.1",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "7.0.2",
  ).map(_ % "test")

}
