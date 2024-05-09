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

package test.uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.libs.json.JsObject
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.StorageConnector
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser._
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.StorageService.utrStorageFormat
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.StorageStub
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class StorageConnectorISpec extends ComponentSpecHelper with StorageStub {

  private val storageConnector = app.injector.instanceOf[StorageConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val utrKey = "utr"

  s"storeDataField($testJourneyId, $utrKey)" should {
    "return SuccessfullyStored" in {
      stubStoreUtr(testJourneyId, Sautr(testSautr))(status = OK)
      val result = await(storageConnector.storeDataField[Utr](testJourneyId, utrKey, Sautr(testSautr)))

      result mustBe SuccessfullyStored
    }
  }

  s"retrieveDataField($testJourneyId, $utrKey)" should {
    "return utr" when {
      "there is utr stored against the journeyId" in {
        stubRetrieveUtr(testJourneyId)(
          status = OK,
          body = testSautrJson
        )

        val result = await(storageConnector.retrieveDataField[JsObject](testJourneyId, utrKey))

        result mustBe Some(testSautrJson)
      }
    }

    "return None" when {
      "there is no utr stored against the journeyId" in {
        stubRetrieveUtr(testJourneyId)(status = NOT_FOUND)

        val result = await(storageConnector.retrieveDataField[JsObject](testJourneyId, utrKey))

        result mustBe None
      }
    }
  }

  s"removeDataField($testJourneyId, $utrKey)" should {
    "return SuccessfullyRemoved" when {
      "the utr successfully removed from the database" in {
        stubRemoveUtr(testJourneyId)(NO_CONTENT)
        val result = await(storageConnector.removeDataField(testJourneyId, utrKey))

        result mustBe SuccessfullyRemoved
      }
    }
  }

  "removeAllData" should {
    "return SuccessfullyRemoved" in {
      stubRemoveAllData(testJourneyId)(NO_CONTENT)

      val result = await(storageConnector.removeAllData(testJourneyId))

      result mustBe SuccessfullyRemoved
    }
  }

  "given BusinessVerificationPass retrieveUADetails" should {
    "return a UADetails" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJson)

      val result = await(storageConnector.retrieveUADetails(testJourneyId))

      result.get mustBe UADetails(
        optUtr = Some(Ctutr(testCtutr)),
        optCtPostcode = Some(testPostcode),
        optChrn = None,
        optIdentifiersMatch = Some(SuccessfulMatch),
        optBusinessVerificationStatus = Some(BusinessVerificationPass),
        optRegistrationStatus = Some(Registered(testSafeId))
      )
    }
  }

  "given BusinessVerificationPass but Registration failed retrieveUADetails" should {
    "return a UADetails" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataWithRegistrationFailedJson)

      val result = await(storageConnector.retrieveUADetails(testJourneyId)).get

      result.optUtr mustBe Some(Ctutr(testCtutr))
      result.optCtPostcode mustBe Some(testPostcode)
      result.optChrn mustBe None
      result.optIdentifiersMatch mustBe Some(SuccessfulMatch)
      result.optBusinessVerificationStatus mustBe Some(BusinessVerificationPass)
      result.optRegistrationStatus match {
        case Some(RegistrationFailed(failures)) => failures mustBe testRegistrationFailure
        case _ => fail("test returned the wrong Registration result")
      }
    }
  }

  "given BusinessVerificationNotEnoughInfoToCallKey retrieveUADetails" should {
    "return a UADetails" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJsonNotFound)

      val result = await(storageConnector.retrieveUADetails(testJourneyId))

      result.get mustBe UADetails(
        optUtr = Some(Ctutr(testCtutr)),
        optCtPostcode = Some(testPostcode),
        optChrn = None,
        optIdentifiersMatch = Some(DetailsNotFound),
        optBusinessVerificationStatus = Some(BusinessVerificationNotEnoughInformationToCallBV),
        optRegistrationStatus = Some(RegistrationNotCalled)
      )
    }
  }

  "given no data retrieveUADetails" should {
    "return a an empty UADetails" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, JsObject.empty)

      val result = await(storageConnector.retrieveUADetails(testJourneyId))

      result.get mustBe UADetails(
        optUtr = None,
        optCtPostcode = None,
        optChrn = None,
        optIdentifiersMatch = None,
        optBusinessVerificationStatus = None,
        optRegistrationStatus = None
      )
    }
  }

  "given BusinessVerificationPass retrieveTrustsDetails" should {
    "return a TrustDetails" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, testTrustJourneyDataJson)

      val result = await(storageConnector.retrieveTrustsDetails(testJourneyId))

      result.get mustBe TrustDetails(
        optUtr = Some(Sautr(testSautr)),
        optSaPostcode = Some(testSaPostcode),
        optChrn = None,
        optIdentifiersMatch = Some(SuccessfulMatch),
        optBusinessVerificationStatus = Some(BusinessVerificationPass),
        optRegistrationStatus = Some(Registered(testSafeId))
      )
    }
  }

  "given BusinessVerificationPass but Registration failed retrieveTrustsDetails" should {
    "return a TrustDetails" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, testTrustJourneyDataWithRegistrationFailedJson)

      val result = await(storageConnector.retrieveTrustsDetails(testJourneyId)).get

      result.optUtr mustBe Some(Sautr(testSautr))
      result.optSaPostcode mustBe Some(testSaPostcode)
      result.optChrn mustBe None
      result.optIdentifiersMatch mustBe Some(SuccessfulMatch)
      result.optBusinessVerificationStatus mustBe Some(BusinessVerificationPass)
      result.optRegistrationStatus match {
        case Some(RegistrationFailed(failures)) => failures mustBe testRegistrationFailure
        case _ => fail("test returned the wrong Registration result")
      }
    }
  }

  "given BusinessVerificationNotEnoughInfoToCallKey retrieveTrustsDetails" should {
    "return a TrustDetails" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, testTrustIdFalseJourneyDataJson)

      val result = await(storageConnector.retrieveTrustsDetails(testJourneyId))

      result.get mustBe TrustDetails(
        optUtr = Some(Sautr(testSautr)),
        optSaPostcode = Some(testSaPostcode),
        optChrn = None,
        optIdentifiersMatch = Some(DetailsMismatch),
        optBusinessVerificationStatus = Some(BusinessVerificationNotEnoughInformationToCallBV),
        optRegistrationStatus = Some(RegistrationNotCalled)
      )
    }
  }

  "given no data retrieveTrustsDetails" should {
    "return a an empty TrustDetails" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, JsObject.empty)

      val result = await(storageConnector.retrieveTrustsDetails(testJourneyId))

      result.get mustBe TrustDetails(
        optUtr = None,
        optSaPostcode = None,
        optChrn = None,
        optIdentifiersMatch = None,
        optBusinessVerificationStatus = None,
        optRegistrationStatus = None
      )
    }
  }

  "retrieveOverseasCompanyDetails" should {
    "return a defined instance of OverseasCompanyDetails when data values exist" in {
      stubRetrieveEntityDetails(testJourneyId)(OK, testOverseasJourneyDataJson(testCtutrJson))

      val result = await(storageConnector.retrieveOverseasDetails(testJourneyId))

      result.get mustBe OverseasCompanyDetails(
        optUtr = Some(Ctutr(testCtutr)),
        optOverseasTaxIdentifier = Some(testOverseasTaxIdentifier),
        optOverseasTaxIdentifierCountry = Some(testOverseasTaxIdentifiersCountry)
      )
    }
    "return None when no overseas company details have been provided" in {
      stubRetrieveEntityDetails(testJourneyId)(NOT_FOUND)

      val result = await(storageConnector.retrieveOverseasDetails(testJourneyId))

      result mustBe None
    }
  }

}
