/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.minorentityidentificationfrontend.config

import play.api.{Configuration, Environment}
import play.api.libs.json.Json
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.Country
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{FeatureSwitching, TrustVerificationStub}

import javax.inject.{Inject, Singleton}
import scala.collection.JavaConverters.asScalaBufferConverter

@Singleton
class AppConfig @Inject()(config: Configuration,
                          servicesConfig: ServicesConfig, environment: Environment) extends FeatureSwitching {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  lazy val allowedHosts: Set[String] = config.underlying.getStringList("microservice.hosts.allowList").asScala.toSet

  lazy val timeToLiveSeconds: Long = servicesConfig.getInt("mongodb.timeToLiveSeconds").toLong

  lazy val defaultServiceName: String = servicesConfig.getString("defaultServiceName")

  lazy val selfBaseUrl: String = servicesConfig.baseUrl("self")
  lazy val selfUrl: String = servicesConfig.getString("microservice.services.self.url")

  private lazy val contactHost: String = servicesConfig.getString("contact-frontend.host")

  private lazy val backendUrl: String = servicesConfig.baseUrl("minor-entity-identification")

  lazy val createJourneyUrl: String = s"$backendUrl/minor-entity-identification/journey"

  lazy val vatRegExitSurveyOrigin: String = "vat-registration"
  private lazy val feedbackUrl: String = servicesConfig.getString("feedback.host")
  lazy val vatRegFeedbackUrl: String = s"$feedbackUrl/feedback/$vatRegExitSurveyOrigin"

  def betaFeedbackUrl(serviceIdentifier: String): String = s"$contactHost/contact/beta-feedback?service=$serviceIdentifier"

  private lazy val businessVerificationUrl = servicesConfig.getString("microservice.services.business-verification.url")

  def createBusinessVerificationJourneyUrl: String = {
    if (isEnabled(BusinessVerificationStub))
      s"$selfBaseUrl/identify-your-trust/test-only/business-verification/journey"
    else
      s"$businessVerificationUrl/journey"
  }

  def getBusinessVerificationResultUrl(journeyId: String): String = {
    if (isEnabled(BusinessVerificationStub))
      s"$selfBaseUrl/identify-your-trust/test-only/business-verification/journey/$journeyId/status"
    else
      s"$businessVerificationUrl/journey/$journeyId/status"
  }

  lazy val accessibilityStatementPath: String = servicesConfig.getString("accessibility-statement.host")
  lazy val vatRegAccessibilityStatementUrl: String = s"$accessibilityStatementPath/accessibility-statement/vat-registration"

  def reportAProblemPartialUrl(serviceIdentifier: String): String =
    s"$contactHost/contact/problem_reports_ajax?service=$serviceIdentifier"

  def reportAProblemNonJSUrl(serviceIdentifier: String): String =
    s"$contactHost/contact/problem_reports_nonjs?service=$serviceIdentifier"

  def minorEntityIdentificationUrl(journeyId: String): String = s"$backendUrl/minor-entity-identification/journey/$journeyId"

  lazy val trustsUrl: String = servicesConfig.baseUrl("trusts")

  def retrieveTrustsKnownFactsUrl(sautr: String): String = {
    val baseUrl: String = if (isEnabled(TrustVerificationStub)) s"$selfBaseUrl/identify-your-trust/test-only" else trustsUrl
    baseUrl + s"/trusts/$sautr/refresh"
  }

  lazy val validateUnincorporatedAssociationDetailsUrl: String = s"$backendUrl/minor-entity-identification/validate-details"

  lazy val countries: Map[String, Country] = {
    environment.resourceAsStream("/countries.json") match {
      case Some(countriesStream) =>
        Json.parse(countriesStream).as[Map[String, Country]]
      case None =>
        throw new InternalServerException("Country list missing")
    }
  }

  lazy val orderedCountryList: Seq[Country] = countries.values.toSeq.sortBy(_.name)

  def getCountryName(countryCode: String): String = countries.get(countryCode) match {
    case Some(Country(_, name)) =>
      name
    case None =>
      throw new InternalServerException("Invalid country code")

  }

  def registerUrl: String = s"$backendUrl/minor-entity-identification/register"

}
