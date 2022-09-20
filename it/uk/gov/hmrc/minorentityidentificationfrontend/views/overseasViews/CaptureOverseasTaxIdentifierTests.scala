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

package uk.gov.hmrc.minorentityidentificationfrontend.views.overseasViews

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureOverseasTaxIdentifier => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testDefaultServiceName, testSignOutUrl, testTechnicalHelpUrl}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.{ElementExtensions, findElementByAttrValue}

trait CaptureOverseasTaxIdentifierTests {
  this: ComponentSpecHelper =>

  def testOverseasTaxIdentifierCommonViewTests(result: => WSResponse): Unit = {

    lazy val config: AppConfig = app.injector.instanceOf[AppConfig]

    lazy val doc: Document = Jsoup.parse(result.body)

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getElementsByClass("govuk-link").get(1).attr("href") mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      doc.title mustBe expectedTitle(doc, messages.title)
    }

    "have a back link" in {
      val backLinks: Elements = doc.getBackLinks

      backLinks.size mustBe 1

      backLinks.first.text mustBe Base.back
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.title
    }

    "have the correct hint text" in {
      val hints: Elements = doc.getHints

      hints.size mustBe 1

      hints.first.text mustBe messages.hint
    }

    "have the correct label for the conditional input field" in {

      val labels: Elements = doc.getLabelElement

      val taxIdentifierElement: Option[Element] = findElementByAttrValue(labels, 0, "for", "tax-identifier")

      taxIdentifierElement match {
        case Some(element) => element.text() mustBe messages.form_field_1
        case None => fail("Unable to find label for tax-identifier")
      }

    }

    "have a save and continue button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a link to contact frontend" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }

  }

  def testInitialCaptureOverseasTaxIdentifierView(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "display neither radio button as being selected" in {

      val radioButton1: Element = doc.getElementById("tax-identifier-radio")

      radioButton1.toString must not include "checked"

      val radioButton2: Element = doc.getElementById("tax-identifier-radio-2")

      radioButton2.toString must not include "checked"
    }

    "have no text in the conditional tax identifier input component" in {

      val taxIdentifier: Element = doc.getElementById("tax-identifier")

      taxIdentifier.hasText mustBe false
    }
  }

  def testCaptureOverseasTaxIdentifierSelectionErrorMessages(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.get(0).text mustBe messages.Error.no_tax_identifier_selection
    }

    "correctly display the field error" in {

      doc.getFieldErrorMessage.first.text() mustBe Base.Error.error + messages.Error.no_tax_identifier_selection
    }

  }

  def testCaptureCaptureOverseasTaxIdentifierUndefinedTaxIdentifierErrorMessages(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.first.text mustBe messages.Error.no_entry_tax_identifier
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.first.text() mustBe Base.Error.error + messages.Error.no_entry_tax_identifier
    }
  }

  def testCaptureCaptureOverseasTaxIdentifiersErrorMessagesInvalidIdentifier(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.first.text mustBe messages.Error.invalid_tax_identifier
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.first.text mustBe Base.Error.error + messages.Error.invalid_tax_identifier
    }
  }

  def testCaptureCaptureOverseasTaxIdentifiersErrorMessagesTooLongIdentifier(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.get(0).text mustBe messages.Error.invalid_length_tax_identifier
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.get(0).text mustBe Base.Error.error + messages.Error.invalid_length_tax_identifier
    }
  }

  def testServiceName(serviceName: String, result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the service name" in {
      doc.getServiceName.text mustBe serviceName
    }
  }

}
