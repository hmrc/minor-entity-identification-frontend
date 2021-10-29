/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.test.Helpers.{await, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{OverseasCompany, Trusts, UnincorporatedAssociation}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec
  extends AnyWordSpec
    with Matchers
    with IdiomaticMockito {

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockJourneyService: JourneyService = mock[JourneyService]
  val mockStorageService: StorageService = mock[StorageService]

  object TestAuditService extends AuditService(mockAuditConnector, mockJourneyService, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "auditJourney" should {
    "send an event" when {
      "the entity is minor and overseas with SA Utr." in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(OverseasCompany))
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(testSaUtr))

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result mustBe()

        mockAuditConnector.sendExplicitAudit("OverseasCompanyRegistration", testOverseasSAUtrAuditEventJson) was called
      }
    }
    "send an event" when {
      "the entity is minor and overseas with CT Utr." in {
        mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(OverseasCompany))
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(testCtUtr))

        val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

        result mustBe()

        mockAuditConnector.sendExplicitAudit("OverseasCompanyRegistration", testOverseasCTUtrAuditEventJson) was called
      }
    }

    "send an event for an Unincorporated Association" in {
      mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(UnincorporatedAssociation))
      mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(None)

      val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

      result mustBe()

      mockAuditConnector.sendExplicitAudit("UnincorporatedAssociationRegistration", testUnincorporatedAssociationAuditEventJson) was called
    }

    "send an event for Trust" in {
      mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
      mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(None)

      val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

      result mustBe()

      mockAuditConnector.sendExplicitAudit("TrustsRegistration", testTrustsAuditEventJson) was called
    }
  }
}
