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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.http.Status.{FORBIDDEN, UNAUTHORIZED}
import play.api.libs.json.JsObject
import play.api.test.Helpers.{CREATED, NOT_FOUND, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers.{routes => uaRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.OverseasCompany
import uk.gov.hmrc.minorentityidentificationfrontend.models.PageConfig
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.BusinessVerificationStub
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class CreateBusinessVerificationJourneyConnectorISpec extends ComponentSpecHelper with BusinessVerificationStub with FeatureSwitching {

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private lazy val createBusinessVerificationJourneyConnector = app.injector.instanceOf[CreateBusinessVerificationJourneyConnector]

  override def beforeEach(): Unit = {
    disable(BusinessVerificationStub)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(BusinessVerificationStub)
  }

  "createBusinessVerificationJourneyConnector" when {

    val expectedUABVCallJson = testCreateBusinessVerificationUAJourneyJson(
      utrJson = testBVCtUtrJson(testCtutr),
      continueUrlForBVCall = uaRoutes.BusinessVerificationController.retrieveBusinessVerificationResult(testJourneyId),
      journeyConfig = testUAJourneyConfig.copy(regime = testRegime.toLowerCase))

    val expectedUABVCallJsonWithCallingService = testCreateBusinessVerificationUAJourneyJson(
      utrJson = testBVCtUtrJson(testCtutr),
      continueUrlForBVCall = uaRoutes.BusinessVerificationController.retrieveBusinessVerificationResult(testJourneyId),
      journeyConfig = testUAJourneyConfigWithCallingService.copy(regime = testRegime.toLowerCase))

    val expectedTrustBVCallJson: JsObject = testCreateBusinessVerificationTrustJourneyJson(
      utrJson = testBVSaUtrJson(testSautr),
      continueUrlForBVCall = trustRoutes.BusinessVerificationController.retrieveBusinessVerificationResult(testJourneyId),
      journeyConfig = testTrustsJourneyConfig.copy(regime = testRegime.toLowerCase))

    val expectedTrustBVCallJsonWithCallingService: JsObject = testCreateBusinessVerificationTrustJourneyJson(
      utrJson = testBVSaUtrJson(testSautr),
      continueUrlForBVCall = trustRoutes.BusinessVerificationController.retrieveBusinessVerificationResult(testJourneyId),
      journeyConfig = testTrustsJourneyConfigWithCallingService.copy(regime = testRegime.toLowerCase))

    s"the $BusinessVerificationStub feature switch is enabled (we will invoke the test backend)" when {
      "the journey creation has been successful" when {
        "the journey is for a Trust business entity" should {
          "return the redirectUri" in {
            enable(BusinessVerificationStub)

            stubCreateBusinessVerificationJourneyFromStub(expBody = expectedTrustBVCallJson)(status = CREATED, body = testBVRedirectURIJson(testContinueUrlToPassToBVCall))

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig))

            result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrlToPassToBVCall))

            verifyCreateBusinessVerificationJourneyFromStub(expectedTrustBVCallJson)
          }
        }
        "the journey is for a Trust business entity and the calling service name has been specified" should {
          "return the redirect uri" in {
            enable(BusinessVerificationStub)

            stubCreateBusinessVerificationJourneyFromStub(expBody = expectedTrustBVCallJsonWithCallingService)(status = CREATED, body = testBVRedirectURIJson(testContinueUrlToPassToBVCall))

            val result =
              await(createBusinessVerificationJourneyConnector
                .createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfigWithCallingService
                  .copy(pageConfig = PageConfig(
                    optServiceName = Some(testDefaultServiceName),
                    deskProServiceId = testDeskProServiceId,
                    signOutUrl = testSignOutUrl,
                    accessibilityUrl = testAccessibilityUrl
                  ))
                )
              )

            result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrlToPassToBVCall))

            verifyCreateBusinessVerificationJourneyFromStub(expectedTrustBVCallJsonWithCallingService)
          }
        }
        "the journey is for a UA business entity" should {
          "return the redirectUri" in {
            enable(BusinessVerificationStub)
            stubCreateBusinessVerificationJourneyFromStub(expBody = expectedUABVCallJson)(CREATED, body = testBVRedirectURIJson(testContinueUrlToPassToBVCall))

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfig))

            result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrlToPassToBVCall))

            verifyCreateBusinessVerificationJourneyFromStub(expectedUABVCallJson)
          }
        }
        "the journey is for a UA business entity and the calling service name has been specified" should {
          "return the redirect uri" in {
            enable(BusinessVerificationStub)
            stubCreateBusinessVerificationJourneyFromStub(expBody = expectedUABVCallJsonWithCallingService)(CREATED, body = testBVRedirectURIJson(testContinueUrlToPassToBVCall))

            val result = await(createBusinessVerificationJourneyConnector
              .createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfigWithCallingService
                .copy(pageConfig = PageConfig(
                  optServiceName = Some(testDefaultServiceName),
                  deskProServiceId = testDeskProServiceId,
                  signOutUrl = testSignOutUrl,
                  accessibilityUrl = testAccessibilityUrl
                ))
              ))

            result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrlToPassToBVCall))

            verifyCreateBusinessVerificationJourneyFromStub(expectedUABVCallJsonWithCallingService)
          }
        }
      }
      "return no redirect URL and an appropriate BV status" when {
        "the journey is for a Trust business entity" when {
          "the journey creation has been unsuccessful because BV cannot find the record" in {
            enable(BusinessVerificationStub)
            stubCreateBusinessVerificationJourneyFromStub(expBody = expectedTrustBVCallJson)(NOT_FOUND)

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig))

            result mustBe Left(NotEnoughEvidence)

            verifyCreateBusinessVerificationJourneyFromStub(expectedTrustBVCallJson)
          }
          "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {
            enable(BusinessVerificationStub)
            stubCreateBusinessVerificationJourneyFromStub(expBody = expectedTrustBVCallJson)(FORBIDDEN)

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig))

            result mustBe Left(UserLockedOut)

            verifyCreateBusinessVerificationJourneyFromStub(expectedTrustBVCallJson)
          }
        }

        "the journey is for a UA business entity" when {
          "the journey creation has been unsuccessful because BV cannot find the record" in {
            enable(BusinessVerificationStub)
            stubCreateBusinessVerificationJourneyFromStub(expBody = expectedUABVCallJson)(NOT_FOUND)

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfig))

            result mustBe Left(NotEnoughEvidence)

            verifyCreateBusinessVerificationJourneyFromStub(expectedUABVCallJson)
          }

          "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {
            enable(BusinessVerificationStub)
            stubCreateBusinessVerificationJourneyFromStub(expBody = expectedUABVCallJson)(FORBIDDEN)

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfig))

            result mustBe Left(UserLockedOut)

            verifyCreateBusinessVerificationJourneyFromStub(expectedUABVCallJson)
          }
        }
      }
    }
    s"the $BusinessVerificationStub feature switch is disabled (we will invoke the real backend)" when {
      "the journey creation has been successful" when {
        "the journey is for a Trust business entity" should {
          "return the redirectUri" in {

            stubCreateBusinessVerificationJourney(expBody = expectedTrustBVCallJson)(status = CREATED, body = testBVRedirectURIJson(testContinueUrlToPassToBVCall))

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig))

            result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrlToPassToBVCall))

            verifyCreateBusinessVerificationJourney(expectedTrustBVCallJson)
          }
        }
        "the journey is for a Trust business entity and the calling service has been specified" should {
          "return the redirect uri" in {

            stubCreateBusinessVerificationJourney(expBody = expectedTrustBVCallJsonWithCallingService)(status = CREATED, body = testBVRedirectURIJson(testContinueUrlToPassToBVCall))

            val result = await(createBusinessVerificationJourneyConnector
              .createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfigWithCallingService
                .copy(pageConfig = PageConfig(
                  optServiceName = Some(testDefaultServiceName),
                  deskProServiceId = testDeskProServiceId,
                  signOutUrl = testSignOutUrl,
                  accessibilityUrl = testAccessibilityUrl
                ))
              ))

            result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrlToPassToBVCall))

            verifyCreateBusinessVerificationJourney(expectedTrustBVCallJsonWithCallingService)
          }
        }
        "the journey is for a UA business entity" should {
          "return the redirectUri" in {

            stubCreateBusinessVerificationJourney(expBody = expectedUABVCallJson)(CREATED, body = testBVRedirectURIJson(testContinueUrlToPassToBVCall))

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfig))

            result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrlToPassToBVCall))

            verifyCreateBusinessVerificationJourney(expectedUABVCallJson)
          }
        }
        "the journey is for a UA business entity and the calling service has been specified" should {
          "return the redirect uri" in {
            stubCreateBusinessVerificationJourney(expBody = expectedUABVCallJsonWithCallingService)(CREATED, body = testBVRedirectURIJson(testContinueUrlToPassToBVCall))

            val result = await(createBusinessVerificationJourneyConnector
              .createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfigWithCallingService.copy(pageConfig = PageConfig(
                optServiceName = Some(testDefaultServiceName),
                deskProServiceId = testDeskProServiceId,
                signOutUrl = testSignOutUrl,
                accessibilityUrl = testAccessibilityUrl
              ))
              ))

            result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrlToPassToBVCall))

            verifyCreateBusinessVerificationJourney(expectedUABVCallJsonWithCallingService)
          }
        }
      }
      "return no redirect URL and an appropriate BV status" when {
        "the journey is for a Trust business entity" when {
          "the journey creation has been unsuccessful because BV cannot find the record" in {

            stubCreateBusinessVerificationJourney(expBody = expectedTrustBVCallJson)(NOT_FOUND)

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig))

            result mustBe Left(NotEnoughEvidence)

            verifyCreateBusinessVerificationJourney(expectedTrustBVCallJson)
          }
          "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {

            stubCreateBusinessVerificationJourney(expBody = expectedTrustBVCallJson)(FORBIDDEN)

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig))

            result mustBe Left(UserLockedOut)

            verifyCreateBusinessVerificationJourney(expectedTrustBVCallJson)
          }
        }

        "the journey is for a UA business entity" when {
          "the journey creation has been unsuccessful because BV cannot find the record" in {

            stubCreateBusinessVerificationJourney(expBody = expectedUABVCallJson)(NOT_FOUND)

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfig))

            result mustBe Left(NotEnoughEvidence)

            verifyCreateBusinessVerificationJourney(expectedUABVCallJson)
          }

          "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {

            stubCreateBusinessVerificationJourney(expBody = expectedUABVCallJson)(FORBIDDEN)

            val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfig))

            result mustBe Left(UserLockedOut)

            verifyCreateBusinessVerificationJourney(expectedUABVCallJson)
          }
        }
      }
    }
    "BV returns an HTTP status different from CREATED, NOT_FOUND or FORBIDDEN" when {
      "the journey is for a Trust business entity throws an exception" in {

        stubCreateBusinessVerificationJourney(expBody = expectedTrustBVCallJson)(UNAUTHORIZED)

        val theActualException: InternalServerException = intercept[InternalServerException] {
          await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig))
        }

        theActualException.getMessage mustBe "Business Verification API failed with status: 401"
      }
      "the journey is for a UA business entity throws an exception" in {

        stubCreateBusinessVerificationJourney(expBody = expectedUABVCallJson)(UNAUTHORIZED)

        val theActualException: InternalServerException = intercept[InternalServerException] {
          await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr, testUAJourneyConfig))
        }

        theActualException.getMessage mustBe "Business Verification API failed with status: 401"
      }
    }
    "called with an OverseasCompany throws an exception" in {
      val theActualException: IllegalArgumentException = intercept[IllegalArgumentException] {
        await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr, testTrustsJourneyConfig.copy(businessEntity = OverseasCompany)))
      }
      theActualException.getMessage mustBe "Only Trusts and UnincorporatedAssociation business entities are supported."
    }
  }

}
