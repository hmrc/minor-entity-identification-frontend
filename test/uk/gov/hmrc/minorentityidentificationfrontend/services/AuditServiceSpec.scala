/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.minorentityidentificationfrontend.services

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks.MockAuditConnector
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{OverseasCompany, Trusts}
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks.{MockJourneyService, MockStorageService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec
  extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with MockJourneyService
    with MockStorageService
    with MockAuditConnector {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object TestAuditService extends AuditService(appConfig, mockAuditConnector, mockJourneyService, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "auditJourney" should {
    "send an event" when {
      "the entity is an OverseasCompany with an SA UTR" in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(OverseasCompany))
        mockStorageService.retrieveOverseasAuditDetails(testJourneyId, testOverseasJourneyConfig()) returns Future.successful(testOverseasSautrAuditDataJson)

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        mockAuditConnector.sendExplicitAudit("OverseasCompanyRegistration", testOverseasSAUtrAuditEventJson) was called
      }
      "the entity is an OverseasCompany with an overseas tax identifier" in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(OverseasCompany))
        mockStorageService.retrieveOverseasAuditDetails(testJourneyId, testOverseasJourneyConfig()) returns Future.successful(testOverseasTaxIdentifierDataJson)

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        mockAuditConnector.sendExplicitAudit("OverseasCompanyRegistration", testOverseasTaxIdentifierAuditEventJson) was called
      }

      "the entity is a Unincorporated Association" in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testUnincorporatedAssociationJourneyConfig())
        mockStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()) returns Future.successful(testNoIdentifiersDataJson)

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        mockAuditConnector.sendExplicitAudit("UnincorporatedAssociationRegistration", testUnincorporatedAssociationAuditEventJson) was called
      }

      "the entity is a Trust" in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
        mockStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()) returns Future.successful(testTrustsDataJson)

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
          saUtr = testSautr,
          saPostCode = testSaPostcode,
          identifiersMatch = "true",
          bvStatus = "success",
          regStatus = "success"
        )

        mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
      }
    }
  }
}
