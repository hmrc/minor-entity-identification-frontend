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
import uk.gov.hmrc.minorentityidentificationfrontend.models._
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
      mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationNotCalled))
      val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

      result.mustBe(())

      mockAuditConnector.sendExplicitAudit("UnincorporatedAssociationRegistration", testUnincorporatedAssociationAuditEventJson) was called
    }

    "send an event" when {

      "the entity type is Trust" when {

        "the user's details provide a successful match" when {

          "the user supplies an Sa Utr and postcode" when {

            "business verification is successful" should {

              "audit a successful registration correctly" in {
                mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
                mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
                mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
                mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(SuccessfulMatch))
                mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(Some(BusinessVerificationPass))
                mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(Registered(testSafeId)))

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

              "audit an unsuccessful registration correctly" in {

                mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
                mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
                mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
                mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(SuccessfulMatch))
                mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(Some(BusinessVerificationPass))
                mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationFailed))

                val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()))

                result.mustBe(())

                val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
                  saUtr = testSautr,
                  saPostCode = testSaPostcode,
                  identifiersMatch = "true",
                  bvStatus = "success",
                  regStatus = "fail"
                )

                mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
              }

            }

            "business verification is not successful" should {

              "audit the failed business verification correctly" in {

                mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
                mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
                mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
                mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(SuccessfulMatch))
                mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(Some(BusinessVerificationFail))
                mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationNotCalled))

                val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

                result.mustBe(())

                val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
                  saUtr = testSautr,
                  saPostCode = testSaPostcode,
                  identifiersMatch = "true",
                  bvStatus = "fail",
                  regStatus = "not called"
                )

                mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
              }
            }

            "business verification returns with there is not enough information to challenge" should {

              "audit the information returned by business verification correctly" in {

                mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
                mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
                mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
                mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(SuccessfulMatch))
                mockStorageService.retrieveBusinessVerificationStatus(testJourneyId).returns(Future.successful(
                  Some(BusinessVerificationNotEnoughInformationToChallenge)))
                mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationNotCalled))

                val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

                result.mustBe(())

                val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
                  saUtr = testSautr,
                  saPostCode = testSaPostcode,
                  identifiersMatch = "true",
                  bvStatus = "Not Enough Information to challenge",
                  regStatus = "not called"
                )

                mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
              }
            }

            "business verification is not requested" should {

              "audit a successful registration successfully" in {

                mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns
                  Future.successful(testJourneyConfig(Trusts, businessVerificationCheck = false))
                mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
                mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
                mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(SuccessfulMatch))
                mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(Registered(testSafeId)))

                val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

                result.mustBe(())

                val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
                  saUtr = testSautr,
                  saPostCode = testSaPostcode,
                  identifiersMatch = "true",
                  bvStatus = "not requested",
                  regStatus = "success"
                )

                mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
              }

              "audit a failed registration correctly" in {

                mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns
                  Future.successful(testJourneyConfig(Trusts, businessVerificationCheck = false))
                mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
                mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
                mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(SuccessfulMatch))
                mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationFailed))

                val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

                result.mustBe(())

                val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
                  saUtr = testSautr,
                  saPostCode = testSaPostcode,
                  identifiersMatch = "true",
                  bvStatus = "not requested",
                  regStatus = "fail"
                )

                mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
              }
            }
          }

          "the user supplies a SA Utr only but the trust is located abroad" when {

            "business verification is successful" should {

              "audit a successful registration successfully" in {

                mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
                mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
                mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
                mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(SuccessfulMatch))
                mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(Some(BusinessVerificationPass))
                mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(Registered(testSafeId)))

                val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

                result.mustBe(())

                val expectedAuditData: JsObject = testSaUtrOnlyTrustsAuditEventJson(
                  identifiersMatch = "true",
                  bvStatus = "success",
                  regStatus = "success"
                )

                mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
              }
            }
          }
        }

        "the user's details do not provide a match" when {

          "the user provides a SA Utr and postcode, but the post code is not matched" should {

            "audit there not being enough information to call business verification correctly" in {

              mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
              mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
              mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
              mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
              mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(DetailsMismatch))
              mockStorageService.retrieveBusinessVerificationStatus(testJourneyId).returns(
                Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
              mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationNotCalled))

              val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

              result.mustBe(())

              val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
                saUtr = testSautr,
                saPostCode = testSaPostcode,
                identifiersMatch = "false",
                bvStatus = "Not enough information to call BV",
                regStatus = "not called"
              )

              mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
            }

          }

          "the user provides a SA Utr and postcode, but the post code lookup returns not found" should {

            "audit journey correctly with identifiersMatch set to 'false'" in {

              mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
              mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr1)))
              mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
              mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
              mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(DetailsNotFound))
              mockStorageService.retrieveBusinessVerificationStatus(testJourneyId).returns(
                Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
              mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationNotCalled))

              val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

              result.mustBe(())

              val expectedAuditData: JsObject = testSaUtrAndPostcodeTrustsAuditEventJson(
                saUtr = testSautr1,
                saPostCode = testSaPostcode,
                identifiersMatch = "false",
                bvStatus = "Not enough information to call BV",
                regStatus = "not called"
              )

              mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called
            }

          }
        }

        "the user's details are not sufficient to attempt a match" when {

          "the user provides a CHRN only" should {

            "audit the journey with 'identifiersMatch' set to 'unmatchable'" in {

              mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
              mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(None)
              mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(None)
              mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))
              mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(UnMatchableWithoutRetry))
              mockStorageService.retrieveBusinessVerificationStatus(testJourneyId).returns(
                Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
              mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationNotCalled))

              val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

              result.mustBe(())

              val expectedAuditData: JsObject = testCHRNOnlyTrustsAuditEventJson(
                identifiersMatch = "unmatchable",
                bvStatus = "Not enough information to call BV",
                regStatus = "not called"
              )

              mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called

            }
          }

          "the user provides neither a SA Utr nor a CHRN" should {

            "audit the journey with 'identifiersMatch' set to 'unmatchable'" in {

              mockJourneyService.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(testJourneyConfig(Trusts))
              mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(None)
              mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(None)
              mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(None)
              mockStorageService.retrieveIdentifiersMatch(testJourneyId) returns Future.successful(Some(UnMatchableWithRetry))
              mockStorageService.retrieveBusinessVerificationStatus(testJourneyId).returns(
                Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
              mockStorageService.retrieveRegistrationStatus(testJourneyId) returns Future.successful(Some(RegistrationNotCalled))

              val result: Unit = await(TestAuditService.auditJourney(testJourneyId, testInternalId))

              result.mustBe(())

              val expectedAuditData: JsObject = testCHRNOnlyTrustsAuditEventJson(
                identifiersMatch = "unmatchable",
                bvStatus = "Not enough information to call BV",
                regStatus = "not called"
              )

              mockAuditConnector.sendExplicitAudit("TrustsRegistration", expectedAuditData) was called

            }

          }

        }
      }
    }
  }
}
