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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnincorporatedAssociationSubmissionServiceSpec
  extends AnyWordSpec
    with Matchers
    with MockValidateUnincorporatedAssociationDetailsService
    with MockStorageService
    with MockAuditService {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val retryUrl: String = "/retry"
  val expectedContinueUrl: String = testContinueUrl + s"?journeyId=$testJourneyId"

  object TestUnincorporatedAssociationSubmissionService extends
    UnincorporatedAssociationSubmissionService(mockAuditService, mockStorageService, mockValidateUnincorporatedAssociationDetailsService)

  "The unincorporated association submission service" should {

    "return the continue url provided by the calling service when the identifiers match" in {

      mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Ctutr(testCtutr)))
      mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testOfficePostcode))
      mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)

      mockValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
        testJourneyId,
        Some(testCtutr),
        Some(testOfficePostcode)
      ) returns Future.successful(SuccessfulMatch)

      mockAuditService.auditJourney(testJourneyId, testUnincorporatedAssociationJourneyConfig()) returns Future.successful(())

      val result = await(TestUnincorporatedAssociationSubmissionService.submit(
        testJourneyId,
        testUnincorporatedAssociationJourneyConfig(),
        retryUrl
      ))

      result mustBe expectedContinueUrl

      mockAuditService.auditJourney(testJourneyId, testUnincorporatedAssociationJourneyConfig()) was called
    }

    "return the retry url for the unincorporated association when the identifiers do not match" in {

      mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Ctutr(testCtutr)))
      mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testOfficePostcode))
      mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)

      mockValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
        testJourneyId,
        Some(testCtutr),
        Some(testOfficePostcode)
      ) returns Future.successful(DetailsMismatch)

      mockAuditService.auditJourney(testJourneyId, testUnincorporatedAssociationJourneyConfig()) returns Future.successful(())

      val result = await(TestUnincorporatedAssociationSubmissionService.submit(
        testJourneyId,
        testUnincorporatedAssociationJourneyConfig(),
        retryUrl
      ))

      result mustBe retryUrl

      mockAuditService.auditJourney(testJourneyId, testUnincorporatedAssociationJourneyConfig()) was called
    }

    "return the retry url for the unincorporated association when the association's details cannot be found" in {

      mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Ctutr(testCtutr)))
      mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testOfficePostcode))
      mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)

      mockValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
        testJourneyId,
        Some(testCtutr),
        Some(testOfficePostcode)
      ) returns Future.successful(DetailsMismatch)

      mockAuditService.auditJourney(testJourneyId, testUnincorporatedAssociationJourneyConfig()) returns Future.successful(())

      val result = await(TestUnincorporatedAssociationSubmissionService.submit(
        testJourneyId,
        testUnincorporatedAssociationJourneyConfig(),
        retryUrl
      ))

      result mustBe retryUrl

      mockAuditService.auditJourney(testJourneyId, testUnincorporatedAssociationJourneyConfig()) was called
    }

    "return the continue url provided by the calling service when the unincorporated association does not have a utr" in {

      mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(None)
      mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(None)
      mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

      mockValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
        testJourneyId,
        None,
        None
      ) returns Future.successful(UnMatchable)

      val result = await(TestUnincorporatedAssociationSubmissionService.submit(
        testJourneyId,
        testUnincorporatedAssociationJourneyConfig(),
        retryUrl
      ))

      result mustBe expectedContinueUrl

      mockAuditService.auditJourney(testJourneyId, testUnincorporatedAssociationJourneyConfig()) was called
    }

  }

}
