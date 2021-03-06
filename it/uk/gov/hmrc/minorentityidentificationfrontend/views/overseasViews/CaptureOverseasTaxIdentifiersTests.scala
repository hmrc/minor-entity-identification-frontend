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
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureOverseasTaxIdentifiers => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testDefaultServiceName, testSignOutUrl, testTechnicalHelpUrl}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait CaptureOverseasTaxIdentifiersTests {
  this: ComponentSpecHelper =>

  def testCaptureCaptureOverseasTaxIdentifiersView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]


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
      doc.title mustBe s"${messages.title} - $testDefaultServiceName - GOV.UK"
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
      doc.getParagraphs.get(1).text mustBe messages.hint
    }

    "have correct labels in the form" in {
      doc.getLabelElement.first.text() mustBe messages.form_field_1
      doc.getLabelElement.get(1).text() mustBe messages.form_field_2
    }

    "have a correct skip link" in {
      doc.getElementById("no-overseas-tax-identifiers").text() mustBe messages.no_identifierLink
    }

    "have a save and continue button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a link to contact frontend" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }

  def testCaptureCaptureOverseasTaxIdentifiersErrorMessages(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.get(0).text mustBe messages.Error.no_entry_tax_identifier + " " + messages.Error.no_entry_country
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.first.text() mustBe Base.Error.error + messages.Error.no_entry_tax_identifier
      doc.getFieldErrorMessage.get(1).text mustBe Base.Error.error + messages.Error.no_entry_country
    }
  }

  def testCaptureCaptureOverseasTaxIdentifiersErrorMessagesInvalidIdentifier(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.get(0).text mustBe messages.Error.invalid_tax_identifier
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.get(0).text mustBe Base.Error.error + messages.Error.invalid_tax_identifier
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


}
