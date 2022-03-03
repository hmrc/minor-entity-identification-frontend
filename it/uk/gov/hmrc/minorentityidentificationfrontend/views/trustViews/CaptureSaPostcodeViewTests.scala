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

package uk.gov.hmrc.minorentityidentificationfrontend.views.trustViews

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureSaPostcode => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.testSignOutUrl
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait CaptureSaPostcodeViewTests {
  this: ComponentSpecHelper =>

  def testCaptureSaPostcodeView(result: => WSResponse): Unit = {
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
      doc.title mustBe messages.title
    }

    "have the correct hint text" in {
      doc.getParagraphs.get(1).text mustBe messages.hint
    }

    "have a correct link to skip self assessment postcode" in {
      doc.getElementById("no-sa-postcode").text() mustBe messages.no_postcodeLink
    }

    "have a save and continue button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a link to contact frontend" in {
      doc.getLink("get-help").text mustBe Base.getHelp
    }
  }

  def testCaptureSaPostcodeErrorMessageInvalidPostcode(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.invalid_sa_postcode
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalid_sa_postcode
    }
  }

  def testCaptureSaPostcodeErrorMessageNoEntryPostcode(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.no_entry_sa_postcode
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.no_entry_sa_postcode
    }
  }
}
