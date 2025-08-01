/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.minorentityidentificationfrontend.views.trustViews

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureCHRN => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testDefaultServiceName, testSignOutUrl, testTechnicalHelpUrl}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.CaptureCHRN


trait CaptureCHRNumberViewTests {
  this: ComponentSpecHelper =>

  def testCaptureCHRNView(result: => WSResponse): Unit = {

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

    "have the correct page header" in {
      val headers: Elements = doc.getElementsByTag("h1")

      headers.size mustBe >= (1)

      headers.first.text mustBe messages.title
    }

    "have the correct inset text" in {

      val insetElements: Elements = doc.getElementsByClass("govuk-inset-text")

      insetElements.size mustBe 1

      insetElements.first.text mustBe messages.inset
    }

    "have the correct hint text" in {
      doc.getElementById("chrn-hint").text mustBe CaptureCHRN.hint
    }

    "have the correct first line with the link" in {
      doc.getParagraphs.get(1).text must include (CaptureCHRN.paragraph1)

      val link = doc.getParagraphs.get(1).select("a")

      link.size mustBe 1

      link.attr("href") mustBe "https://www.gov.uk/find-charity-information"
      link.text() must include ("(opens in new tab)")
      link.attr("target") mustBe "_blank"
      link.attr("rel") must include ("noopener")
      link.attr("rel") must include ("noreferrer")

    }

    "have the correct second line" in {
      doc.getParagraphs.get(2).text mustBe messages.paragraph2
    }

    "have the correct label" in {
      doc.getLabelElement.first().text() mustBe messages.label
    }

    "have an input text box with the identifier 'chrn'" in {

      val optInput: Option[Element] = Option(doc.getElementById("chrn"))

      optInput match {
        case Some(input) => input.attr("type") mustBe "text"
        case None => fail("""Input element "chrn" cannot be found""")
      }
    }

    "have a link to enable users to skip to check your answers page" in {

      val optLink: Option[Element] = Option(doc.getElementById("no-chrn"))

      optLink match {
        case Some(link) => link.text mustBe messages.link
        case None => fail(s"""Link "no-chrn" cannot be found""")
      }
    }

    "have a continue button" in {
      doc.getSubmitButton.first.text mustBe Base.continue
    }

    "have a link to contact frontend" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }

  }

  def testCaptureCHRNErrorMessageNotEntered(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.error_not_entered
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.error_not_entered
    }

  }

  def testCaptureCHRNErrorMessageInvalidCharacters(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.error_invalid_characters
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.error_invalid_characters
    }

  }

  def testCaptureCHRNErrorMessageMaximumLengthExceeded(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.error_invalid_length
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.error_invalid_length
    }

  }

  def testServiceName(serviceName: String, result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the service name" in {
      doc.getServiceName.text mustBe serviceName
    }
  }

}
