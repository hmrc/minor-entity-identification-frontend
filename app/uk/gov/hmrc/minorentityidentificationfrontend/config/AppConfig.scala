/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.BusinessVerificationStub
import uk.gov.hmrc.minorentityidentificationfrontend.models.Country
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{FeatureSwitching, TrustVerificationStub}

import java.io.IOException
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters.CollectionHasAsScala
@Singleton
class AppConfig @Inject()(config: Configuration,
                          servicesConfig: ServicesConfig, environment: Environment) extends FeatureSwitching {

  lazy val appName: String = servicesConfig.getString("appName")

  lazy val allowedHosts: Set[String] = config.underlying.getStringList("microservice.hosts.allowList").asScala.toSet

  lazy val timeToLiveSeconds: Long = servicesConfig.getInt("mongodb.timeToLiveSeconds").toLong

  lazy val defaultServiceName: String = servicesConfig.getString("defaultServiceName")

  lazy val selfBaseUrl: String = servicesConfig.baseUrl("self")
  lazy val selfUrl: String = servicesConfig.getString("microservice.services.self.url")

  lazy val contactHost: String = servicesConfig.getString("contact-frontend.host")

  lazy val grsDeskProServiceId: String = "grs"

  private lazy val backendUrl: String = servicesConfig.baseUrl("minor-entity-identification")

  lazy val createJourneyUrl: String = s"$backendUrl/minor-entity-identification/journey"

  lazy val vatRegExitSurveyOrigin: String = "vat-registration"
  private lazy val feedbackUrl: String = servicesConfig.getString("feedback.host")
  lazy val vatRegFeedbackUrl: String = s"$feedbackUrl/feedback/$vatRegExitSurveyOrigin"
  lazy val basGatewayUrl: String = s"${servicesConfig.baseUrl("bas-gateway-frontend")}"

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

  private lazy val countriesListInEnglish: Map[String, Country] = getCountryList("/countries.json")

  private lazy val countriesListInWelsh: Map[String, Country] = getCountryList("/countries_cy.json")

  private lazy val orderedCountryListInEnglish: Seq[Country] = countriesListInEnglish.values.toSeq.sortBy(_.name)

  private lazy val orderedCountryListInWelsh: Seq[Country] =  countriesListInWelsh.values.toSeq.sortBy(_.name)

  private def getCountryListByLanguage(code: String): Map[String, Country] = if(code == "cy") countriesListInWelsh else countriesListInEnglish

  def getOrderedCountryListByLanguage(code: String = "en"): Seq[Country] = if(code == "cy") orderedCountryListInWelsh else orderedCountryListInEnglish

  def getCountryName(countryCode: String, langCode: String = "en"): String = getCountryListByLanguage(langCode).get(countryCode) match {
    case Some(Country(_, name)) =>
      name
    case None =>
      throw new InternalServerException("Invalid country code")
  }

  def getCountryList(fileName: String) : Map[String, Country] = {

    environment.resourceAsStream(fileName) match {
      case Some(countriesStream) =>
        try {
          Json.parse(countriesStream).as[Map[String, Country]]
        } finally {
          try {
            countriesStream.close()
          } catch {
            case ex : IOException =>
              throw new InternalServerException(s"I/O exception raised on closing file $fileName : ${ex.getMessage}")
          }
        }
      case None => throw new InternalServerException(s"Country list file $fileName cannot be found")
    }

  }

  def registerTrustUrl: String = s"$backendUrl/minor-entity-identification/register-trust"

  def registerUAUrl: String = s"$backendUrl/minor-entity-identification/register-ua"

}
