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

package uk.gov.hmrc.minorentityidentificationfrontend.views.uaViews

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, CheckYourAnswers => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testCtutr, testJourneyId, testOfficePostcode}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

import scala.collection.JavaConverters.asScalaIteratorConverter

trait UaCheckYourAnswersSpecificViewTests {

  this: ComponentSpecHelper =>

  def testUaWithCtutrAndOfficePostcodeSummaryListView(response: => WSResponse, journeyId: String): Unit = {
    lazy val summaryListRows: List[Element] = extractSummaryListRows(response)

    "have a summary list which" should {

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a utr row" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.uaUtr
        utrRow.getSummaryListAnswer mustBe testCtutr
        utrRow.getSummaryListChangeLink mustBe uaControllers.routes.CaptureCtutrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.uaUtr}"
      }

      "have an registered office postcode row" in {
        val postcodeRow = summaryListRows(1)

        postcodeRow.getSummaryListQuestion mustBe messages.uaPostcode
        postcodeRow.getSummaryListAnswer mustBe testOfficePostcode
        postcodeRow.getSummaryListChangeLink mustBe uaControllers.routes.CaptureOfficePostcodeController.show(journeyId).url
        postcodeRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.uaPostcode}"
      }
    }

  }

  def testUaWithNoCtutrAndNoCHRNSummaryListView(response: => WSResponse, journeyId: String): Unit = {
    lazy val summaryListRows: List[Element] = extractSummaryListRows(response)

    "have a summary list which" should {

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a Ctutr row saying utr not provided" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.uaUtr
        utrRow.getSummaryListAnswer mustBe messages.noUtr
        utrRow.getSummaryListChangeLink mustBe uaControllers.routes.CaptureCtutrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.uaUtr}"
      }

      "have an charity HRMC reference number row saying charity HRMC reference number not provided" in {
        val charityHRMCReferenceNumberRow = summaryListRows.last

        charityHRMCReferenceNumberRow.getSummaryListQuestion mustBe messages.charityHRMCReferenceNumber
        charityHRMCReferenceNumberRow.getSummaryListAnswer mustBe messages.charityHRMCReferenceNumberNotProvided
        charityHRMCReferenceNumberRow.getSummaryListChangeLink mustBe uaControllers.routes.CaptureCHRNController.show(journeyId).url
        charityHRMCReferenceNumberRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.charityHRMCReferenceNumber}"
      }
    }

  }

  private def extractSummaryListRows(result: => WSResponse): List[Element] =
    Jsoup.parse(result.body).getSummaryListRows.iterator().asScala.toList


}
