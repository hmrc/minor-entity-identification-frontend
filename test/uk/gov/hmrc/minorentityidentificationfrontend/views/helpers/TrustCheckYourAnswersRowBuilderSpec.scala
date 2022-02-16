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

package uk.gov.hmrc.minorentityidentificationfrontend.views.helpers

import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.{testCharityHMRCReferenceNumber, testJourneyId, testPostcode, testSaUtr}

class TrustCheckYourAnswersRowBuilderSpec extends AbstractCheckYourAnswersRowBuilderSpec {

  val rowBuilderUnderTest: TrustCheckYourAnswersRowBuilder = new TrustCheckYourAnswersRowBuilder()

  val testNoCharityHMRCReferenceNumberRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("HMRC reference number")),
    value = Value(HtmlContent("The charity does not have a HMRC reference number")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = trustControllers.routes.CaptureCHRNController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("HMRC reference number")
      )
    )))
  )

  val testCharityHMRCReferenceNumberRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("HMRC reference number")),
    value = Value(HtmlContent(testCharityHMRCReferenceNumber)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = trustControllers.routes.CaptureCHRNController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("HMRC reference number")
      )
    )))
  )

  val testPostcodeRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("Self Assessment postcode")),
    value = Value(HtmlContent(testPostcode)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = trustControllers.routes.CaptureSaPostcodeController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Self Assessment postcode")
      )
    )))
  )

  val testNoPostcodeRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("Self Assessment postcode")),
    value = Value(HtmlContent("The business does not have a Self Assessment postcode")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = trustControllers.routes.CaptureSaPostcodeController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Self Assessment postcode")
      )
    )))
  )

  "buildSummaryListRows" should {
    "build a summary list sequence" when {
      "the user enter a SAUTR and postcode (with SAUTR defined, charity HMRC reference number cannot be entered)" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optUtr = Some(testSaUtr),
          optPostcode = Some(testPostcode),
          optCharityHMRCReferenceNumber = None
        )(messages)

        actualSummaryList mustBe Seq(
          testUtrRow(changeValuePageLink = trustControllers.routes.CaptureSautrController.show(testJourneyId)),
          testPostcodeRow
        )

      }

      "the user enter a SAUTR and they did NOT enter a postcode (with SAUTR defined, charity HMRC reference number cannot be entered)" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optUtr = Some(testSaUtr),
          optPostcode = None,
          optCharityHMRCReferenceNumber = None
        )(messages)

        actualSummaryList mustBe Seq(
          testUtrRow(changeValuePageLink = trustControllers.routes.CaptureSautrController.show(testJourneyId)),
          testNoPostcodeRow
        )

      }

      "the user did NOT enter a SAUTR and they did NOT enter a charity HMRC reference number (with No SAUTR defined, postcode cannot be entered)" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optUtr = None,
          optPostcode = None,
          optCharityHMRCReferenceNumber = None
        )(messages)

        actualSummaryList mustBe Seq(
          testNoUtrRow(changeValuePageLink = trustControllers.routes.CaptureSautrController.show(testJourneyId)),
          testNoCharityHMRCReferenceNumberRow
        )

      }

      "the user did NOT enter a SAUTR and they did enter a charity HMRC reference number (with No SAUTR defined, postcode cannot be entered)" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optUtr = None,
          optPostcode = None,
          optCharityHMRCReferenceNumber = Some(testCharityHMRCReferenceNumber)
        )(messages)

        actualSummaryList mustBe Seq(
          testNoUtrRow(changeValuePageLink = trustControllers.routes.CaptureSautrController.show(testJourneyId)),
          testCharityHMRCReferenceNumberRow
        )

      }

    }
  }

  "buildSummaryListRows" should {
    "throws exception" when {
      "user has a SAUTR and a charity HMRC reference number (with or without postcode)" in {

        List(None, Some(testPostcode)).map(optPostCode => {

          val theActualException: IllegalStateException = intercept[IllegalStateException] {
            rowBuilderUnderTest.buildSummaryListRows(
              journeyId = testJourneyId,
              optUtr = Some(testSaUtr),
              optPostcode = optPostCode,
              optCharityHMRCReferenceNumber = Some(testCharityHMRCReferenceNumber)
            )(messages)
          }

          theActualException.getMessage mustBe "User cannot have SAUTR and charity HMRC reference number at the same time"

        })
      }
      "user has a postcode and they dont have SAUTR (with or without CharityHMRCReferenceNumber)" in {

        List(None, Some(testCharityHMRCReferenceNumber)).map(optCharityHMRCReferenceNumber => {

          val theActualException: IllegalStateException = intercept[IllegalStateException] {
            rowBuilderUnderTest.buildSummaryListRows(
              journeyId = testJourneyId,
              optUtr = None,
              optPostcode = Some(testPostcode),
              optCharityHMRCReferenceNumber = optCharityHMRCReferenceNumber
            )(messages)
          }

          theActualException.getMessage mustBe "User cannot have postcode when they dont have SAUTR"

        })
      }
    }
  }

}
