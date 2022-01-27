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

package uk.gov.hmrc.minorentityidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CheckYourAnswers => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testJourneyId, testSignOutUrl, testUtr}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

import scala.collection.JavaConverters.asScalaIteratorConverter

trait CheckYourAnswersViewTests {
  this: ComponentSpecHelper =>

  def testCheckYourAnswersView(result: => WSResponse): Unit = {
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

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.title
    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 1 row" in {
        summaryListRows.size mustBe 1
      }

      "have a utr row" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.utr
        utrRow.getSummaryListAnswer mustBe testUtr
        utrRow.getSummaryListChangeLink mustBe routes.CaptureUtrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.utr}"
      }
    }

    "have a continue and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.confirmAndContinue
    }

    "have a link to contact frontend" in {
      doc.getLink("get-help").text mustBe Base.getHelp
    }
  }

  def testCheckYourAnswersNoUtrView(result: => WSResponse): Unit = {
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

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.title
    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 1 row" in {
        summaryListRows.size mustBe 1
      }

      "have a utr row" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.utr
        utrRow.getSummaryListAnswer mustBe messages.noUtr
        utrRow.getSummaryListChangeLink mustBe routes.CaptureUtrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.utr}"
      }
    }

    "have a continue and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.confirmAndContinue
    }

    "have a link to contact frontend" in {
      doc.getLink("get-help").text mustBe Base.getHelp
    }
  }

}
