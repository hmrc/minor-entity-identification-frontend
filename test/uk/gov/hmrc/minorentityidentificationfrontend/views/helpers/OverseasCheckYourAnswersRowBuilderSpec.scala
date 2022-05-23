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
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.{testJourneyId, testOverseas, testSautr}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Overseas, Sautr}

class OverseasCheckYourAnswersRowBuilderSpec extends AbstractCheckYourAnswersRowBuilderSpec {

  val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val rowBuilderUnderTest = new OverseasCheckYourAnswersRowBuilder()

  val testOverseasTaxIdentifiersRow = SummaryListRow(
    key = Key(content = Text("Overseas tax identifier")),
    value = Value(HtmlContent(s"${testOverseas.taxIdentifier}<br>${mockAppConfig.getCountryName(testOverseas.country)}")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = overseasControllers.routes.CaptureOverseasTaxIdentifiersController.show(testJourneyId).url,
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
        href = overseasControllers.routes.CaptureOverseasTaxIdentifiersController.show(testJourneyId).url,
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

        actualSummaryList mustBe Seq(
          testUtrRow(changeValuePageLink = overseasControllers.routes.CaptureUtrController.show(testJourneyId)),
          testOverseasTaxIdentifiersRow
        )

      }
      "the user could entered utr and overseas tax identifier buy they did not" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optOverseasTaxId = None,
          optUtr = None
        )(messages, mockAppConfig)

        actualSummaryList mustBe Seq(
          testNoUtrRow(changeValuePageLink = overseasControllers.routes.CaptureUtrController.show(testJourneyId)),
          testNoOverseasTaxIdentifiersRow
        )

      }
    }

    "build a summary list sequence containing cy translation" when {
      "there is a cookie specifying cy language" in {
        val incomingRequest = FakeRequest().withCookies(Cookie("PLAY_LANG","cy"))
        val messagesInWelsh: Messages = app.injector.instanceOf[MessagesApi].preferred(incomingRequest)

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optOverseasTaxId = Some(Overseas(taxIdentifier = "134124532", country = "AF")),
          optUtr = Some(Sautr(testSautr))
        )(messagesInWelsh, mockAppConfig)

        actualSummaryList.size must  be(2)

        actualSummaryList(1) must be(
          SummaryListRow(
            key = Key(content = Text("Dynodydd treth tramor")),
            value = Value(HtmlContent(s"${testOverseas.taxIdentifier}<br>Affganistan")),
            actions = Some(Actions(items = Seq(
              ActionItem(
                href = overseasControllers.routes.CaptureOverseasTaxIdentifiersController.show(testJourneyId).url,
                content = Text("Newid"),
                visuallyHiddenText = Some("Dynodydd treth tramor")
              )
            )))
          )
        )

      }
    }

  }
}
