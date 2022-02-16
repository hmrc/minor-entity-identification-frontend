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
import org.jsoup.nodes.Element
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, CheckYourAnswers => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testJourneyId, testOverseasTaxIdentifiers, testUtr}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers.routes
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

import scala.collection.JavaConverters.asScalaIteratorConverter

trait OverseasCheckYourAnswersSpecificViewTests {

  this: ComponentSpecHelper =>

  def testOverseasSummaryViewWithUtrAndOverseasTaxIdentifier(result: => WSResponse, journeyId: String): Unit = {
    lazy val summaryListRows: List[Element] = extractSummaryListRows(result)

    "have a summary list which" should {

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a utr row" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.utr
        utrRow.getSummaryListAnswer mustBe testUtr
        utrRow.getSummaryListChangeLink mustBe overseasControllers.routes.CaptureUtrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.utr}"
      }

      "have an overseas tax identifiers row" in {
        val taxIdentifierRow = summaryListRows(1)

        taxIdentifierRow.getSummaryListQuestion mustBe messages.overseasTaxIdentifier
        taxIdentifierRow.getSummaryListAnswer mustBe s"${testOverseasTaxIdentifiers.taxIdentifier} Albania"
        taxIdentifierRow.getSummaryListChangeLink mustBe overseasControllers.routes.CaptureOverseasTaxIdentifiersController.show(journeyId).url
        taxIdentifierRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.overseasTaxIdentifier}"
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
        taxIdentifierRow.getSummaryListAnswer mustBe messages.overseasTaxIdentifierNotProvided
        taxIdentifierRow.getSummaryListChangeLink mustBe overseasControllers.routes.CaptureOverseasTaxIdentifiersController.show(journeyId).url
        taxIdentifierRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.overseasTaxIdentifier}"
      }
    }
  }

  private def extractSummaryListRows(result: => WSResponse): List[Element] =
    Jsoup.parse(result.body).getSummaryListRows.iterator().asScala.toList

}
