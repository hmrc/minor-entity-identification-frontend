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

import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
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
    with MockAuditConnector
    with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockAuditConnector)
    reset(mockJourneyService)
    reset(mockStorageService)
  }

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object TestAuditService extends AuditService(appConfig, mockAuditConnector, mockJourneyService, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "auditJourney" should {
    "send an event" when {
      "the entity is an OverseasCompany with an SA UTR" in {
        when(mockJourneyService.getJourneyConfig(testJourneyId, testInternalId)).thenReturn(Future.successful(testJourneyConfig(OverseasCompany)))
        when(mockStorageService.retrieveOverseasAuditDetails(testJourneyId, testOverseasJourneyConfig()))
          .thenReturn(Future.successful(testOverseasSautrAuditDataJson))

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        verify(mockAuditConnector).sendExplicitAudit("OverseasCompanyRegistration", testOverseasSAUtrAuditEventJson)
      }
      "the entity is an OverseasCompany with an overseas tax identifier" in {
        when(mockJourneyService.getJourneyConfig(testJourneyId, testInternalId)).thenReturn(Future.successful(testJourneyConfig(OverseasCompany)))
        when(mockStorageService.retrieveOverseasAuditDetails(testJourneyId, testOverseasJourneyConfig()))
          .thenReturn(Future.successful(testOverseasTaxIdentifierDataJson))

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        verify(mockAuditConnector).sendExplicitAudit("OverseasCompanyRegistration", testOverseasTaxIdentifierAuditEventJson)
      }

      "the entity is a Unincorporated Association" in {
        when(mockJourneyService.getJourneyConfig(testJourneyId, testInternalId)).thenReturn(Future.successful(testUnincorporatedAssociationJourneyConfig()))
        when(mockStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))
          .thenReturn(Future.successful(testNoIdentifiersDataJson))

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        verify(mockAuditConnector).sendExplicitAudit("UnincorporatedAssociationRegistration", testUnincorporatedAssociationAuditEventJson)
      }

      "the entity is a Trust" in {
        when(mockJourneyService.getJourneyConfig(testJourneyId, testInternalId)).thenReturn(Future.successful(testJourneyConfig(Trusts)))
        when(mockStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig())).thenReturn(Future.successful(testTrustsDataJson))

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
          saUtr = testSautr,
          saPostCode = testSaPostcode,
          identifiersMatch = "true",
          bvStatus = "success",
          regStatus = "success"
        )

        verify(mockAuditConnector).sendExplicitAudit("TrustsRegistration", expectedAuditData)
      }
    }
  }
}
