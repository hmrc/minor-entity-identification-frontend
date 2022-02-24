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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers

import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessVerificationPass
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, BusinessVerificationStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.{ComponentSpecHelper, WiremockHelper}

import javax.inject.Singleton

@Singleton
class BusinessVerificationControllerISpec extends ComponentSpecHelper
  with FeatureSwitching
  with AuthStub
  with BusinessVerificationStub
  with StorageStub
  with BeforeAndAfterEach
  with WiremockHelper {


  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build

  override def beforeEach(): Unit = {
    await(journeyConfigRepository.drop)
    super.beforeEach()
  }

  "GET /business-verification-result" when {
    s"the $BusinessVerificationStub feature switch is enabled" should {
      "redirect to the continue url" when {
        "the user has an sautr" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullTrustJourney)
          enable(BusinessVerificationStub)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK)
          stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
          stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)

          lazy val result = get(s"/identify-your-trust/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
          }

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
        }

        "the user does not have an sautr" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullTrustJourney)
          enable(BusinessVerificationStub)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
          stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)

          lazy val result = get(s"/identify-your-trust/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
          }

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
        }
      }

      "throw an exception when the query string is missing" in {
        enable(EnableFullTrustJourney)
        enable(BusinessVerificationStub)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        lazy val result = get(s"/identify-your-trust/$testJourneyId/business-verification-result")

        result.status mustBe INTERNAL_SERVER_ERROR

      }
    }

    s"the $BusinessVerificationStub feature switch is disabled" should {
      "redirect to the continue url" when {
        "the user has sautr" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK)
          stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
          stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)

          lazy val result = get(s"/identify-your-trust/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
          }

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
        }

        "the user does not have a sautr" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
          stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)

          lazy val result = get(s"/identify-your-trust/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
          }

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)

        }
      }

      "throw an exception when the query string is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        lazy val result = get(s"/identify-your-trust/$testJourneyId/business-verification-result")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
