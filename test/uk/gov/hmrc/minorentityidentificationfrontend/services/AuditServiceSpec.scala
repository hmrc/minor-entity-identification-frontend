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

package uk.gov.hmrc.minorentityidentificationfrontend.services

import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{OverseasCompany, Trusts, UnincorporatedAssociation}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Ctutr, RegistrationNotCalled, Sautr}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec
  extends AnyWordSpec
    with Matchers
    with IdiomaticMockito
    with GuiceOneAppPerSuite {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockJourneyService: JourneyService = mock[JourneyService]
  val mockStorageService: StorageService = mock[StorageService]

  object TestAuditService extends AuditService(appConfig, mockAuditConnector, mockJourneyService, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "auditJourney" should {
    "send an event" when {
      "the entity is an OverseasCompany with SA Utr and no overseas tax identifiers." in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(OverseasCompany))
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrieveOverseasTaxIdentifiers(testJourneyId) returns Future.successful(None)
        mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationNotCalled))

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        mockAuditConnector.sendExplicitAudit("OverseasCompanyRegistration", testOverseasSAUtrAuditEventJson) was called
      }
    }
    "send an event" when {
      "the entity is an OverseasCompany with CT Utr and no overseas tax identifiers." in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(OverseasCompany))
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Ctutr(testCtutr)))
        mockStorageService.retrieveOverseasTaxIdentifiers(testJourneyId) returns Future.successful(None)

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        mockAuditConnector.sendExplicitAudit("OverseasCompanyRegistration", testOverseasCTUtrAuditEventJson) was called
      }
    }
    "send an event" when {
      "the entity is an OverseasCompany and the user provided an overseas tax identifiers." in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(OverseasCompany))
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Ctutr(testCtutr)))
        mockStorageService.retrieveOverseasTaxIdentifiers(testJourneyId) returns Future.successful(Some(testOverseas))

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result.mustBe(())

        val expectedAuditEventJson: JsObject = testOverseasCTUtrAuditEventJson ++ testOverseasIdentifiersAuditEventJson

        mockAuditConnector.sendExplicitAudit(auditType = "OverseasCompanyRegistration", detail = expectedAuditEventJson) was called
      }
    }

    "send an event for an Unincorporated Association" in {
      mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(UnincorporatedAssociation))
      mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(None)

      val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

      result.mustBe(())

      mockAuditConnector.sendExplicitAudit("UnincorporatedAssociationRegistration", testUnincorporatedAssociationAuditEventJson) was called
    }

    "send an event for Trust" in {
      mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
      mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(None)

      val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

      result.mustBe(())

      mockAuditConnector.sendExplicitAudit("TrustsRegistration", testTrustsAuditEventJson) was called
    }
  }
}
