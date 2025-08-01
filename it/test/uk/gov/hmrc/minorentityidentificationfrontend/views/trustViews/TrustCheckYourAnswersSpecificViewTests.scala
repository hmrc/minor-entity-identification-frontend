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
import org.jsoup.nodes.Element
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.MessageLookup.{Base, CheckYourAnswers => messages}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testJourneyId, testSaPostcode, testSautr}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions
import scala.jdk.CollectionConverters._

trait TrustCheckYourAnswersSpecificViewTests {

  this: ComponentSpecHelper =>

  def testTrustWithUtrAndPostcodeSummaryListView(response: => WSResponse, journeyId: String): Unit = {
    lazy val summaryListRows: List[Element] = extractSummaryListRows(response)
    lazy val doc = Jsoup.parse(response.body)

    "have a summary list which" should {

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a utr row" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.utr
        utrRow.getSummaryListAnswer mustBe testSautr
        utrRow.getSummaryListChangeLink mustBe trustControllers.routes.CaptureSautrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.utr}"
      }

      "have an sa postcode row" in {
        val postcodeRow = summaryListRows(1)

        postcodeRow.getSummaryListQuestion mustBe messages.postcode
        postcodeRow.getSummaryListAnswer mustBe testSaPostcode
        postcodeRow.getSummaryListChangeLink mustBe trustControllers.routes.CaptureSaPostcodeController.show(journeyId).url
        postcodeRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.postcode}"
      }
    }

    "must not display extra h2 headers for overseas journey" in {
      val h2s = doc.select("h2.govuk-heading-m").eachText().asScala
      h2s must not contain messages.overseasH2UkDetails
      h2s must not contain messages.overseasH2OverseasDetails
    }

  }

  def testTrustWithNoUtrAndNoCharityHRMCReferenceNumberSummaryListView(response: => WSResponse, journeyId: String): Unit = {
    lazy val summaryListRows: List[Element] = extractSummaryListRows(response)

    "have a summary list which" should {

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a utr row saying utr not provided" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.utr
        utrRow.getSummaryListAnswer mustBe messages.noTrustUtr
        utrRow.getSummaryListChangeLink mustBe trustControllers.routes.CaptureSautrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.utr}"
      }

      "have an charity HRMC reference number row saying charity HRMC reference number not provided" in {
        val charityHRMCReferenceNumberRow = summaryListRows.last

        charityHRMCReferenceNumberRow.getSummaryListQuestion mustBe messages.charityHRMCReferenceNumber
        charityHRMCReferenceNumberRow.getSummaryListAnswer mustBe messages.charityHMRCReferenceNumberNotProvided
        charityHRMCReferenceNumberRow.getSummaryListChangeLink mustBe trustControllers.routes.CaptureCHRNController.show(journeyId).url
        charityHRMCReferenceNumberRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.charityHRMCReferenceNumber}"
      }
    }

  }

  def testTrustWithUtrAndNoPostcodeSummaryListView(response: => WSResponse, journeyId: String): Unit = {

    lazy val summaryListRows: List[Element] = extractSummaryListRows(response)

    "have a summary list which" should {

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a utr row" in {
        val utrRow = summaryListRows.head

        utrRow.getSummaryListQuestion mustBe messages.utr
        utrRow.getSummaryListAnswer mustBe testSautr
        utrRow.getSummaryListChangeLink mustBe trustControllers.routes.CaptureSautrController.show(testJourneyId).url
        utrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.utr}"
      }

      "have an sa post code row indicating no post code has been provided" in {
        val postcodeRow = summaryListRows(1)

        postcodeRow.getSummaryListQuestion mustBe messages.postcode
        postcodeRow.getSummaryListAnswer mustBe messages.noPostCode
        postcodeRow.getSummaryListChangeLink mustBe trustControllers.routes.CaptureSaPostcodeController.show(journeyId).url
        postcodeRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.postcode}"
      }
    }

  }

  private def extractSummaryListRows(result: => WSResponse): List[Element] =
    Jsoup.parse(result.body).getSummaryListRows.iterator().asScala.toList

}
