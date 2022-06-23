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

import org.jsoup.nodes.Document
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.{TableFor1, TableFor2, Tables}
import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config._
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

class ControllersSupportWelshTranslationISpec
  extends ComponentSpecHelper
    with StorageStub
    with AuthStub
    with FeatureSwitching{

  private def trustsBaseUrl = "/identify-your-trust"
  private def overseasBaseUrl = "/identify-your-overseas-business"
  private def uaBaseUrl = "/identify-your-unincorporated-association"

  "all the views requested via GET" when {
    "the entity is Trusts" should {

      object TestPrecondition {
        type TestPrecondition = () => Any
        val checkYourAnswers: TestPrecondition = () => stubRetrieveEntityDetails(testJourneyId)(OK, body = testTrustJourneyDataJson)
        val confirmSautr: TestPrecondition = () => stubRetrieveUtr(testJourneyId)(status = OK, body = testSautrJson)
        val confirmChrn: TestPrecondition = () => stubRetrieveCHRN(testJourneyId)(status = OK, optCharityHMRCReferenceNumber = testCHRN)
        val confirmSaPostcode: TestPrecondition = () => stubRetrievePostcode(testJourneyId)(status = OK, postcode = testSaPostcode)
        val cannotConfirmBusiness: TestPrecondition = () => ()
      }

      val allGETUrlsToBeTested: TableFor2[String, TestPrecondition.TestPrecondition] =
        Tables.Table(
          ("urlToBeTested", "doThisToCreateTestPrecondition"),
          (s"$trustsBaseUrl/$testJourneyId/check-your-answers-business", TestPrecondition.checkYourAnswers),
          (s"$trustsBaseUrl/$testJourneyId/sa-utr", TestPrecondition.confirmSautr),
          (s"$trustsBaseUrl/$testJourneyId/chrn", TestPrecondition.confirmChrn),
          (s"$trustsBaseUrl/$testJourneyId/self-assessment-postcode", TestPrecondition.confirmSaPostcode),
          (s"$trustsBaseUrl/$testJourneyId/cannot-confirm-business", TestPrecondition.cannotConfirmBusiness)
        )

      "display welsh translation when cy cookie is specified" in {

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allGETUrlsToBeTested) { (getUrlToBeTested, doThisToCreateTestPrecondition) =>

          lazy val actualDocFromResponse: Document = {
            doThisToCreateTestPrecondition()
            extractDocumentFrom(aWSResponse = get(getUrlToBeTested, cookie = cyLangCookie))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshServiceName

        }
      }

      "display the custom welsh translation in the pageConfig when cy cookie is specified" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testDefaultWelshJourneyConfig
        ))

        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allGETUrlsToBeTested) { (getUrlToBeTested, doThisToCreateTestPrecondition) =>

          lazy val actualDocFromResponse: Document = {
            doThisToCreateTestPrecondition()
            extractDocumentFrom(aWSResponse = get(getUrlToBeTested, cookie = cyLangCookie))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshJourneyConfig.pageConfig.optLabels.get.welshServiceName
        }
      }

    }
    "the entity is Overseas Company" should {

      object TestPrecondition {
        type TestPrecondition = () => Any
        val checkYourAnswers: TestPrecondition = () => stubRetrieveEntityDetails(testJourneyId)(OK, testOverseasJourneyDataJson(testSautrJson))
        val confirmSautr: TestPrecondition = () => stubRetrieveUtr(testJourneyId)(status = OK, body = testSautrJson)
        val confirmOverseasTaxIdentifiers: TestPrecondition = () => stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
      }

      val allGETUrlsToBeTested: TableFor2[String, TestPrecondition.TestPrecondition] =
        Tables.Table(
          ("urlToBeTested", "doThisToCreateTestPrecondition"),
          (s"$overseasBaseUrl/$testJourneyId/check-your-answers-business", TestPrecondition.checkYourAnswers),
          (s"$overseasBaseUrl/$testJourneyId/non-uk-company-utr", TestPrecondition.confirmSautr),
          (s"$overseasBaseUrl/$testJourneyId/overseas-identifier", TestPrecondition.confirmOverseasTaxIdentifiers)
        )

      "display welsh translation when cy cookie is specified" in {

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allGETUrlsToBeTested) { (getUrlToBeTested, doThisToCreateTestPrecondition) =>

          lazy val actualDocFromResponse: Document = {
            doThisToCreateTestPrecondition()
            extractDocumentFrom(aWSResponse = get(getUrlToBeTested, cookie = cyLangCookie))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshServiceName

        }
      }

      "display the custom welsh translation in the pageConfig when cy cookie is specified" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testDefaultWelshJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allGETUrlsToBeTested) { (getUrlToBeTested, doThisToCreateTestPrecondition) =>

          lazy val actualDocFromResponse: Document = {
            doThisToCreateTestPrecondition()
            extractDocumentFrom(aWSResponse = get(getUrlToBeTested, cookie = cyLangCookie))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshJourneyConfig.pageConfig.optLabels.get.welshServiceName
        }
      }

    }
    "the entity is Unincorporated Association" should {

      object TestPrecondition {
        type TestPrecondition = () => Any
        val checkYourAnswers: TestPrecondition = () => stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJson)
        val confirmCtutr: TestPrecondition = () => stubRetrieveUtr(testJourneyId)(status = OK, body = testCtutrJson)
        val confirmChrn: TestPrecondition = () => stubRetrieveCHRN(testJourneyId)(status = OK, testCHRN)
        val confirmRegisteredOfficePostcode: TestPrecondition = () => stubRetrievePostcode(testJourneyId)(status = OK, testOfficePostcode)
        val cannotConfirmBusiness: TestPrecondition = () => ()
      }

      val allGETUrlsToBeTested: TableFor2[String, TestPrecondition.TestPrecondition] =
        Tables.Table(
          ("urlToBeTested", "doThisToCreateTestPrecondition"),
          (s"$uaBaseUrl/$testJourneyId/check-your-answers-business", TestPrecondition.checkYourAnswers),
          (s"$uaBaseUrl/$testJourneyId/ct-utr", TestPrecondition.confirmCtutr),
          (s"$uaBaseUrl/$testJourneyId/chrn", TestPrecondition.confirmChrn),
          (s"$uaBaseUrl/$testJourneyId/registered-office-postcode", TestPrecondition.confirmRegisteredOfficePostcode),
          (s"$uaBaseUrl/$testJourneyId/cannot-confirm-business", TestPrecondition.cannotConfirmBusiness)
        )

      "display welsh translation when cy cookie is specified" in {

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
        ))

        enable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allGETUrlsToBeTested) { (getUrlToBeTested, doThisToCreateTestPrecondition) =>

          lazy val actualDocFromResponse: Document = {
            doThisToCreateTestPrecondition()
            extractDocumentFrom(aWSResponse = get(getUrlToBeTested, cookie = cyLangCookie))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshServiceName

        }
      }

      "display the custom welsh translation in the pageConfig when cy cookie is specified" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testDefaultWelshJourneyConfig
        ))

        enable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allGETUrlsToBeTested) { (getUrlToBeTested, doThisToCreateTestPrecondition) =>

          lazy val actualDocFromResponse: Document = {
            doThisToCreateTestPrecondition()
            extractDocumentFrom(aWSResponse = get(getUrlToBeTested, cookie = cyLangCookie))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshJourneyConfig.pageConfig.optLabels.get.welshServiceName
        }
      }

    }

  }

  "all the views in case of error (after a POST)" when {
    "the entity is Trusts" should {

      val allPOSTUrlsToBeTested: TableFor1[String] =
        Tables.Table(
          "urlToBeTested",
          s"$trustsBaseUrl/$testJourneyId/sa-utr",
          s"$trustsBaseUrl/$testJourneyId/chrn",
          s"$trustsBaseUrl/$testJourneyId/self-assessment-postcode",
          s"$trustsBaseUrl/$testJourneyId/cannot-confirm-business"
        )

      "display welsh translation when cy cookie is specified" in {

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allPOSTUrlsToBeTested) { urlToBeTested =>

          lazy val actualDocFromResponse: Document = {
            val anInvalidFormToCauseAnError = "somethingWrong" -> ""
            extractDocumentFrom(aWSResponse = post(urlToBeTested, cookie = cyLangCookie)(form = anInvalidFormToCauseAnError))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshServiceName

        }
      }

      "display the custom welsh translation in the pageConfig when cy cookie is specified" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testDefaultWelshJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allPOSTUrlsToBeTested) { urlToBeTested =>

          lazy val actualDocFromResponse: Document = {
            val anInvalidFormToCauseAnError = "somethingWrong" -> ""
            extractDocumentFrom(aWSResponse = post(urlToBeTested, cookie = cyLangCookie)(anInvalidFormToCauseAnError))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshJourneyConfig.pageConfig.optLabels.get.welshServiceName

        }
      }

    }
    "the entity is Overseas Company" should {

      val allPOSTUrlsToBeTested: TableFor1[String] =
        Tables.Table(
          "urlToBeTested",
          s"$overseasBaseUrl/$testJourneyId/non-uk-company-utr",
          s"$overseasBaseUrl/$testJourneyId/overseas-identifier"
        )

      "display welsh translation when cy cookie is specified" in {

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allPOSTUrlsToBeTested) { urlToBeTested =>

          lazy val actualDocFromResponse: Document = {
            val anInvalidFormToCauseAnError = "somethingWrong" -> ""
            extractDocumentFrom(aWSResponse = post(urlToBeTested, cookie = cyLangCookie)(form = anInvalidFormToCauseAnError))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshServiceName

        }
      }

      "display the custom welsh translation in the pageConfig when cy cookie is specified" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testDefaultWelshJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allPOSTUrlsToBeTested) { urlToBeTested =>

          lazy val actualDocFromResponse: Document = {
            val anInvalidFormToCauseAnError = "somethingWrong" -> ""
            extractDocumentFrom(aWSResponse = post(urlToBeTested, cookie = cyLangCookie)(anInvalidFormToCauseAnError))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshJourneyConfig.pageConfig.optLabels.get.welshServiceName

        }
      }

    }
    "the entity is Unincorporated Association" should {

      val allPOSTUrlsToBeTested: TableFor1[String] =
        Tables.Table(
          "urlToBeTested",
          s"$uaBaseUrl/$testJourneyId/ct-utr",
          s"$uaBaseUrl/$testJourneyId/chrn",
          s"$uaBaseUrl/$testJourneyId/registered-office-postcode",
          s"$uaBaseUrl/$testJourneyId/cannot-confirm-business"
        )

      "display welsh translation when cy cookie is specified" in {

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
        ))

        enable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allPOSTUrlsToBeTested) { urlToBeTested =>

          lazy val actualDocFromResponse: Document = {
            val anInvalidFormToCauseAnError = "somethingWrong" -> ""
            extractDocumentFrom(aWSResponse = post(urlToBeTested, cookie = cyLangCookie)(form = anInvalidFormToCauseAnError))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshServiceName

        }
      }

      "display the custom welsh translation in the pageConfig when cy cookie is specified" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          journeyConfig = testDefaultWelshJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        forAll(allPOSTUrlsToBeTested) { urlToBeTested =>

          lazy val actualDocFromResponse: Document = {
            val anInvalidFormToCauseAnError = "somethingWrong" -> ""
            extractDocumentFrom(aWSResponse = post(urlToBeTested, cookie = cyLangCookie)(anInvalidFormToCauseAnError))
          }

          actualDocFromResponse.getServiceName.text mustBe testDefaultWelshJourneyConfig.pageConfig.optLabels.get.welshServiceName

        }
      }

    }
  }
}
