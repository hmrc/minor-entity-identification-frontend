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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.JsObject
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatchingResult.SuccessfulMatchKey
import uk.gov.hmrc.minorentityidentificationfrontend.models.{BusinessVerificationPass, JourneyConfig, Registered}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.{AuditEnabledSpecHelper, WiremockHelper}

trait AbstractBusinessVerificationControllerISpec
  extends AuditEnabledSpecHelper
  with FeatureSwitching
  with AuthStub
  with BusinessVerificationStub
  with StorageStub
  with RegisterStub
  with BeforeAndAfterEach
  with WiremockHelper {

  val businessVerificationResultUrlPrefix: String

  val businessEntityBuilder: () => JourneyConfig

  val retrieveUtrJson: JsObject

  val testUtr: String

  def commonTest(): Unit = {
    s"the $BusinessVerificationStub feature switch is enabled" should {
      "redirect to the continue url" when {
        "the user has an sautr" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            businessEntityBuilder()
          ))
          enable(BusinessVerificationStub)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, retrieveUtrJson)
          stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, testBusinessVerificationPassJson)
          stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)
          stubRegister(testUtr, testRegime)(OK, Registered(testSafeId))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
          stubAudit()
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testSuccessfulRegistrationJson)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, SuccessfulMatchKey)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
          stubRetrievePostcode(testJourneyId)(OK, testSaPostcode)

          lazy val result = get(businessVerificationResultUrlPrefix + s"?journeyId=$testBusinessVerificationJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
          }

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          verifyAudit()
        }
      }

      "throw an exception when the query string is missing" in {
        enable(BusinessVerificationStub)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, testBusinessVerificationPassJson)
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          businessEntityBuilder()
        ))
        stubAudit()

        lazy val result = get(businessVerificationResultUrlPrefix)

        result.status mustBe INTERNAL_SERVER_ERROR

      }
    }
    s"the $BusinessVerificationStub feature switch is disabled" should {
      "redirect to the continue url" when {
        "the user has sautr" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            businessEntityBuilder()
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, retrieveUtrJson)
          stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, testBusinessVerificationPassJson)
          stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)
          stubRegister(testUtr, testRegime)(OK, Registered(testSafeId))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
          stubAudit()
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testSuccessfulRegistrationJson)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, SuccessfulMatchKey)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
          stubRetrievePostcode(testJourneyId)(OK, testSaPostcode)

          lazy val result = get(businessVerificationResultUrlPrefix + s"?journeyId=$testBusinessVerificationJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
          }

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          verifyAudit()
        }
      }

      "throw an exception when the query string is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, testBusinessVerificationPassJson)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          businessEntityBuilder()
        ))
        stubAudit()

        lazy val result = get(businessVerificationResultUrlPrefix)

        result.status mustBe INTERNAL_SERVER_ERROR

      }
    }
  }

}
