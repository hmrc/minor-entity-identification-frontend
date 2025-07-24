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

package uk.gov.hmrc.minorentityidentificationfrontend.views.overseasViews

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testJourneyId, testSautr}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testOverseasTaxIdentifier, testOverseasTaxIdentifiersCountryFullName}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, CheckYourAnswers => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

import scala.collection.JavaConverters.asScalaIteratorConverter

trait OverseasCheckYourAnswersSpecificViewTests {

  this: ComponentSpecHelper =>

  def testOverseasSummaryViewWithUtrAndOverseasTaxIdentifier(result: => WSResponse, journeyId: String): Unit = {

    val changeOverseasTaxIdentifierPageLink: String = overseasControllers.routes.CaptureOverseasTaxIdentifierController.show(journeyId).url
    val changeOverseasTaxIdentifiersCountryPageLink: String = overseasControllers.routes.CaptureOverseasTaxIdentifiersCountryController.show(journeyId).url

    lazy val summaryListRows: List[Element] = extractSummaryListRows(result)
    lazy val doc = Jsoup.parse(result.body)

    "have a summary list which" should {

      "display h2 headers" in {
        val h2s = doc.select("h2.govuk-heading-m")
        h2s.get(0).text() mustBe messages.overseasH2UkDetails
        h2s.get(1).text() mustBe messages.overseasH2OverseasDetails
      }

      "have 3 rows" in {
        summaryListRows.size mustBe 3
      }

      "have a utr row" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.utr
        utrRow.getSummaryListAnswer mustBe testSautr
        utrRow.getSummaryListChangeLink mustBe overseasControllers.routes.CaptureUtrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.utr}"
      }

      "have an overseas tax identifier row" in {
        val taxIdentifierRow = summaryListRows(1)

        taxIdentifierRow.getSummaryListQuestion mustBe messages.overseasTaxIdentifier
        taxIdentifierRow.getSummaryListAnswer mustBe s"Yes, $testOverseasTaxIdentifier"
        taxIdentifierRow.getSummaryListChangeLink mustBe changeOverseasTaxIdentifierPageLink
        taxIdentifierRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.overseasTaxIdentifier}"
      }

      "have an overseas tax identifier country row" in {
        val taxIdentifierCountryRow = summaryListRows.last

        taxIdentifierCountryRow.getSummaryListQuestion mustBe messages.overseasTaxIdentifierCountry
        taxIdentifierCountryRow.getSummaryListAnswer mustBe testOverseasTaxIdentifiersCountryFullName
        taxIdentifierCountryRow.getSummaryListChangeLink mustBe changeOverseasTaxIdentifiersCountryPageLink
        taxIdentifierCountryRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.overseasTaxIdentifierCountry}"
      }
    }

  }

  def testOverseasSummaryViewWithUtrAndOverseasTaxIdentifierNotProvided(result: => WSResponse, journeyId: String): Unit = {
    lazy val summaryListRows: List[Element] = extractSummaryListRows(result)

    "have a summary list which" should {

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a utr row saying utr not provided" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.utr
        utrRow.getSummaryListAnswer mustBe messages.noUtr
        utrRow.getSummaryListChangeLink mustBe overseasControllers.routes.CaptureUtrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.utr}"
      }

      "have an overseas tax identifiers row saying overseas tax identifiers not provided" in {
        val taxIdentifierRow = summaryListRows(1)

        taxIdentifierRow.getSummaryListQuestion mustBe messages.overseasTaxIdentifier
        taxIdentifierRow.getSummaryListAnswer mustBe Base.no
        taxIdentifierRow.getSummaryListChangeLink mustBe overseasControllers.routes.CaptureOverseasTaxIdentifierController.show(journeyId).url
        taxIdentifierRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.overseasTaxIdentifier}"
      }
    }
  }

  private def extractSummaryListRows(result: => WSResponse): List[Element] =
    Jsoup.parse(result.body).getSummaryListRows.iterator().asScala.toList

}
