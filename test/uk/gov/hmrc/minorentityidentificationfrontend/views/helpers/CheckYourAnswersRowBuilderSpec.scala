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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers.routes
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.{testJourneyId, testOverseas, testSautr}
import uk.gov.hmrc.minorentityidentificationfrontend.models.Sautr

class CheckYourAnswersRowBuilderSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val rowBuilderUnderTest = new CheckYourAnswersRowBuilder()

  val testUtrRow = SummaryListRow(
    key = Key(content = Text("Unique taxpayers reference number")),
    value = Value(content = HtmlContent(testSautr)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureUtrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique taxpayers reference number")
      )
    )))
  )

  val testNoUtrRow = SummaryListRow(
    key = Key(content = Text("Unique taxpayers reference number")),
    value = Value(content = HtmlContent("The business does not have a UTR")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureUtrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique taxpayers reference number")
      )
    )))
  )

  val testOverseasTaxIdentifiersRow = SummaryListRow(
    key = Key(content = Text("Overseas tax identifier")),
    value = Value(HtmlContent(s"${testOverseas.taxIdentifier}<br>${mockAppConfig.getCountryName(testOverseas.country)}")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureOverseasTaxIdentifiersController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Overseas tax identifier")
      )
    )))
  )

  val testNoOverseasTaxIdentifiersRow = SummaryListRow(
    key = Key(content = Text("Overseas tax identifier")),
    value = Value(HtmlContent("I do not want to provide an identifier")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureOverseasTaxIdentifiersController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Overseas tax identifier")
      )
    )))
  )

  "build" should {
    "build a summary list sequence" when {
      "there is a utr and an overseas Tax Identifier" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optOverseasTaxId = Some(testOverseas),
          optUtr = Some(Sautr(testSautr))
        )(messages, mockAppConfig)

        actualSummaryList mustBe Seq(testUtrRow, testOverseasTaxIdentifiersRow)

      }

      "the user could entered utr and overseas tax identifier buy they did not" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optOverseasTaxId = None,
          optUtr = None
        )(messages, mockAppConfig)

        actualSummaryList mustBe Seq(testNoUtrRow, testNoOverseasTaxIdentifiersRow)

      }

    }
  }
}
