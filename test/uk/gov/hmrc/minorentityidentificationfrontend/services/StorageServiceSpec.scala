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
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks.MockStorageConnector
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StorageServiceSpec extends AnyWordSpec with Matchers with MockStorageConnector {

  object TestStorageService extends StorageService(mockStorageConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "retrieveOverseasAuditDetails" should {
    "return the correct json" when {
      "a sautr is entered" in {
        mockStorageConnector.retrieveOverseasDetails(testJourneyId) returns
          Future.successful(Some(OverseasCompanyDetails(Some(Sautr(testSautr)), Some(testOverseas))))
        val result = await(TestStorageService.retrieveOverseasAuditDetails(testJourneyId, testOverseasJourneyConfig()))

        result mustBe testOverseasSautrDataJson ++ testOverseasTaxIdentifiersJson
      }
      "a ctutr is entered but no overseas tax identifier" in {
        mockStorageConnector.retrieveOverseasDetails(testJourneyId) returns
          Future.successful(Some(OverseasCompanyDetails(Some(Ctutr(testCtutr)), None)))

        val result = await(TestStorageService.retrieveOverseasAuditDetails(testJourneyId, testOverseasJourneyConfig()))

        result mustBe testOverseasCtutrDataJson
      }
      "only overseas tax identifier is entered" in {
        mockStorageConnector.retrieveOverseasDetails(testJourneyId) returns
          Future.successful(Some(OverseasCompanyDetails(None, Some(testOverseas))))

        val result = await(TestStorageService.retrieveOverseasAuditDetails(testJourneyId, testOverseasJourneyConfig()))

        result mustBe testOverseasTaxIdentifiersDataJson ++ testOverseasTaxIdentifiersJson
      }
      "no data is entered" in {
        mockStorageConnector.retrieveOverseasDetails(testJourneyId) returns Future.successful(None)

        val result = await(TestStorageService.retrieveOverseasAuditDetails(testJourneyId, testOverseasJourneyConfig()))

        result mustBe testOverseasNoIdentifiersDataJson
      }
    }
  }

  "retrieveTrustsAuditDetails" should {
    "return the correct json" when {
      "user is on the legacy journey" in {
        mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(None)

        val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()))

        result mustBe testLegacyDataJson
      }
      "no identifiers are provided" in {
        val testTrustDetails = TrustDetails(
          None,
          None,
          None,
          Some(UnMatchable),
          Some(BusinessVerificationNotEnoughInformationToCallBV),
          Some(RegistrationNotCalled)
        )
        mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(Some(testTrustDetails))

        val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()))

        result mustBe testNoIdentifiersDataJson
      }
      "only chrn provided" in {
        val testTrustDetails = TrustDetails(
          None,
          None,
          Some(testCHRN),
          Some(UnMatchable),
          Some(BusinessVerificationNotEnoughInformationToCallBV),
          Some(RegistrationNotCalled)
        )
        mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(Some(testTrustDetails))

        val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()))

        result mustBe testOnlyCHRNDataJson
      }
      "both sautr and postcode are provided" when {
        "identifiers successfully match and BV and Registration are successful" in {
          val testTrustDetails = TrustDetails(
            Some(Sautr(testSautr)),
            Some(testSaPostcode),
            None,
            Some(SuccessfulMatch),
            Some(BusinessVerificationPass),
            Some(Registered(testSafeId))
          )
          mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(Some(testTrustDetails))

          val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()))

          result mustBe testTrustsDataJson
        }
        "BV fails" in {
          val testTrustDetails = TrustDetails(
            Some(Sautr(testSautr)),
            Some(testSaPostcode),
            None,
            Some(SuccessfulMatch),
            Some(BusinessVerificationFail),
            Some(RegistrationNotCalled)
          )
          mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(Some(testTrustDetails))

          val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()))

          result mustBe testTrustsBvFailedDataJson("fail")
        }
        "BV returns Not Found" in {
          val testTrustDetails = TrustDetails(
            Some(Sautr(testSautr)),
            Some(testSaPostcode),
            None,
            Some(SuccessfulMatch),
            Some(BusinessVerificationNotEnoughInformationToChallenge),
            Some(RegistrationNotCalled)
          )
          mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(Some(testTrustDetails))

          val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()))

          result mustBe testTrustsBvFailedDataJson("Not Enough Information to challenge")
        }
        "Registration fails" in {
          val testTrustDetails = TrustDetails(
            Some(Sautr(testSautr)),
            Some(testSaPostcode),
            None,
            Some(SuccessfulMatch),
            Some(BusinessVerificationPass),
            Some(RegistrationFailed(registrationFailures = Some(Array(Failure("code1", "reason1")))))
          )
          mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(Some(testTrustDetails))

          val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()))

          result mustBe testTrustsRegistrationFailedDataJson
        }
        "Business Verification is not requested" in {
          val testTrustDetails = TrustDetails(
            Some(Sautr(testSautr)),
            Some(testSaPostcode),
            None,
            Some(SuccessfulMatch),
            None,
            Some(Registered(testSafeId))
          )
          mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(Some(testTrustDetails))

          val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig(false)))

          result mustBe testTrustsBvNotRequestedDataJson
        }
      }
      "only a sautr are provided" in {
        val testTrustDetails = TrustDetails(
          Some(Sautr(testSautr)),
          None,
          None,
          Some(SuccessfulMatch),
          Some(BusinessVerificationPass),
          Some(Registered(testSafeId))
        )
        mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(Some(testTrustDetails))

        val result = await(TestStorageService.retrieveTrustsAuditDetails(testJourneyId, testTrustJourneyConfig()))

        result mustBe testTrustsDataJsonNoPostcode
      }
    }
  }

  "retrieveUAAuditDetails" should {
    "return the correct json" when {
      "user is on the legacy journey" in {
        mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(None)

        val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))

        result mustBe testLegacyDataJson
      }
      "no identifiers are provided" in {
        val testUADetails = UADetails(
          None,
          None,
          None,
          Some(UnMatchable),
          Some(BusinessVerificationNotEnoughInformationToCallBV),
          Some(RegistrationNotCalled)
        )
        mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(Some(testUADetails))

        val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))

        result mustBe testNoIdentifiersDataJson
      }
      "only chrn provided" in {
        val testUADetails = UADetails(
          None,
          None,
          Some(testCHRN),
          Some(UnMatchable),
          Some(BusinessVerificationNotEnoughInformationToCallBV),
          Some(RegistrationNotCalled)
        )
        mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(Some(testUADetails))

        val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))

        result mustBe testOnlyCHRNDataJson
      }
      "both ctutr and postcode are provided" when {
        "identifiers successfully match and BV and Registration are successful" in {
          val testUADetails = UADetails(
            Some(Ctutr(testCtutr)),
            Some(testOfficePostcode),
            None,
            Some(SuccessfulMatch),
            Some(BusinessVerificationPass),
            Some(Registered(testSafeId))
          )
          mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(Some(testUADetails))

          val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))

          result mustBe testUADataJson
        }
        "BV fails" in {
          val testUADetails = UADetails(
            Some(Ctutr(testCtutr)),
            Some(testOfficePostcode),
            None,
            Some(SuccessfulMatch),
            Some(BusinessVerificationFail),
            Some(RegistrationNotCalled)
          )
          mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(Some(testUADetails))

          val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))

          result mustBe testUABvFailedDataJson("fail")
        }
        "BV returns Not Found" in {
          val testUADetails = UADetails(
            Some(Ctutr(testCtutr)),
            Some(testOfficePostcode),
            None,
            Some(SuccessfulMatch),
            Some(BusinessVerificationNotEnoughInformationToChallenge),
            Some(RegistrationNotCalled)
          )
          mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(Some(testUADetails))

          val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))

          result mustBe testUABvFailedDataJson("Not Enough Information to challenge")
        }
        "Registration fails" in {
          val testUADetails = UADetails(
            Some(Ctutr(testCtutr)),
            Some(testOfficePostcode),
            None,
            Some(SuccessfulMatch),
            Some(BusinessVerificationPass),
            Some(RegistrationFailed(registrationFailures = Some(Array(Failure("code1", "reason1")))))
          )
          mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(Some(testUADetails))

          val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))

          result mustBe testUARegistrationFailedDataJson
        }
        "Business Verification is not requested" in {
          val testUADetails = UADetails(
            Some(Ctutr(testCtutr)),
            Some(testOfficePostcode),
            None,
            Some(SuccessfulMatch),
            None,
            Some(Registered(testSafeId))
          )
          mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(Some(testUADetails))

          val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig(false)))

          result mustBe testUABvNotRequestedDataJson
        }
      }
      "only a ctutr are provided" in {
        val testUADetails = UADetails(
          Some(Ctutr(testCtutr)),
          None,
          None,
          Some(SuccessfulMatch),
          Some(BusinessVerificationPass),
          Some(Registered(testSafeId))
        )
        mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(Some(testUADetails))

        val result = await(TestStorageService.retrieveUAAuditDetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))

        result mustBe testUADataJsonNoPostcode
      }
    }
  }

  "retrieveTrustDetails" should {
    "return the correct json" when {

      "user is on the legacy journey" in {
        mockStorageConnector.retrieveTrustsDetails(testJourneyId) returns Future.successful(None)

        val theActualException: InternalServerException  = intercept[InternalServerException] {
          await(TestStorageService.retrieveTrustsDetails(testJourneyId, testTrustJourneyConfig()))
        }

        theActualException.getMessage mustBe "No Trusts journey data stored for journeyId: " + testJourneyId
      }

    }
  }

  "retrieveUADetails" should {
    "return the correct json" when {

      "user is on the legacy journey" in {
        mockStorageConnector.retrieveUADetails(testJourneyId) returns Future.successful(None)

        val theActualException: InternalServerException = intercept[InternalServerException] {
          await(TestStorageService.retrieveUADetails(testJourneyId, testUnincorporatedAssociationJourneyConfig()))
        }

        theActualException.getMessage mustBe "No UA journey data stored for journeyId: " + testJourneyId
      }
    }

  }

}
