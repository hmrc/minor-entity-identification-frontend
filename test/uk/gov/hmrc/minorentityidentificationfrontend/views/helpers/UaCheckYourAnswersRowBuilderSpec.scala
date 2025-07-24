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

package uk.gov.hmrc.minorentityidentificationfrontend.views.helpers

import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.{testOfficePostcode, _}
import uk.gov.hmrc.minorentityidentificationfrontend.models.Ctutr

class UaCheckYourAnswersRowBuilderSpec extends AbstractCheckYourAnswersRowBuilderSpec {

  val rowBuilderUnderTest: UaCheckYourAnswersRowBuilder = new UaCheckYourAnswersRowBuilder()

  val testCtutrNumberRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("Unique Taxpayer Reference (UTR)")),
    value = Value(HtmlContent(testCtutr)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = uaControllers.routes.CaptureCtutrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique Taxpayer Reference (UTR)")
      )
    )))
  )

  val testNoCtutrNumberRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("Unique Taxpayer Reference (UTR)")),
    value = Value(HtmlContent("The association does not have a UTR")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = uaControllers.routes.CaptureCtutrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique Taxpayer Reference (UTR)")
      )
    )))
  )

  val testCharityHMRCReferenceNumberRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("HMRC reference number")),
    value = Value(HtmlContent(testCHRN)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = uaControllers.routes.CaptureCHRNController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("HMRC reference number")
      )
    )))
  )

  val testNoCharityHMRCReferenceNumberRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("HMRC reference number")),
    value = Value(HtmlContent("The association does not have a HMRC reference number")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = uaControllers.routes.CaptureCHRNController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("HMRC reference number")
      )
    )))
  )

  val testOfficePostcodeRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("Corporation Tax postcode")),
    value = Value(HtmlContent(testOfficePostcode)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = uaControllers.routes.CaptureOfficePostcodeController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Corporation Tax postcode")
      )
    )))
  )

  "buildSummaryListRows" should {
    "build a summary list sequence" when {
      "the user enter a CTUTR and postcode" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optUtr = Some(Ctutr(testCtutr)),
          optOfficePostcode = Some(testOfficePostcode),
          optCHRN = None
        )(messages)

        actualSummaryList mustBe Seq(
          testCtutrNumberRow,
          testOfficePostcodeRow
        )
      }

      "the user did NOT enter a CTUTR but entered a charity HMRC reference number" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optUtr = None,
          optOfficePostcode = None,
          optCHRN = Some(testCHRN)
        )(messages)

        actualSummaryList mustBe Seq(
          testNoCtutrNumberRow,
          testCharityHMRCReferenceNumberRow
        )
      }

      "the user did NOT enter a CTUTR and they did NOT enter a charity HMRC reference number" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optUtr = None,
          optOfficePostcode = None,
          optCHRN = None
        )(messages)

        actualSummaryList mustBe Seq(
          testNoCtutrNumberRow,
          testNoCharityHMRCReferenceNumberRow
        )
      }
    }
  }

  "buildSummaryListRows" should {
    "throws exception" when {
      "user has a CTUTR and a charity HMRC reference number)" in {

        List(None, Some(testOfficePostcode)).map(optOfficePostCode => {

          val theActualException: IllegalStateException = intercept[IllegalStateException] {
            rowBuilderUnderTest.buildSummaryListRows(
              journeyId = testJourneyId,
              optUtr = Some(Ctutr(testCtutr)),
              optOfficePostcode = optOfficePostCode,
              optCHRN = Some(testCHRN)
            )(messages)
          }

          theActualException.getMessage mustBe "User cannot have CTUTR and charity HMRC reference number at the same time"
        })
      }
      "user has a office postcode and they don't have CTUTR" in {

        List(None, Some(testCHRN)).map(optCHRN => {

          val theActualException: IllegalStateException = intercept[IllegalStateException] {
            rowBuilderUnderTest.buildSummaryListRows(
              journeyId = testJourneyId,
              optUtr = None,
              optOfficePostcode = Some(testOfficePostcode),
              optCHRN = optCHRN
            )(messages)
          }

          theActualException.getMessage mustBe "User cannot have registered office postcode when they don't have CTUTR"
        })
      }
    }
  }

}
