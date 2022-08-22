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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.{testJourneyId, testSautr}
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.{testOverseasTaxIdentifier, testOverseasTaxIdentifierCountry}
import uk.gov.hmrc.minorentityidentificationfrontend.models.Sautr

class OverseasCheckYourAnswersRowBuilderSpec extends AbstractCheckYourAnswersRowBuilderSpec {

  val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val rowBuilderUnderTest = new OverseasCheckYourAnswersRowBuilder()

  val testOverseasTaxIdentifierRow = SummaryListRow(
    key = Key(content = Text("Overseas tax identifier")),
    value = Value(HtmlContent(s"Yes, $testOverseasTaxIdentifier")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = overseasControllers.routes.CaptureOverseasTaxIdentifierController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Overseas tax identifier")
      )
    )))
  )

  val testOverseasTaxIdentifierCountryRow = SummaryListRow(
    key = Key(content = Text("Country of overseas tax identifier")),
    value = Value(HtmlContent(mockAppConfig.getCountryName(testOverseasTaxIdentifierCountry))),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = overseasControllers.routes.CaptureOverseasTaxIdentifiersCountryController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Country of overseas tax identifier")
      )
    )))
  )

  val testNoOverseasTaxIdentifiersRow = SummaryListRow(
    key = Key(content = Text("Overseas tax identifier")),
    value = Value(HtmlContent("No")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = overseasControllers.routes.CaptureOverseasTaxIdentifierController.show(testJourneyId).url,
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
          optOverseasTaxIdentifier = Some(testOverseasTaxIdentifier),
          optOverseasTaxIdentifiersCountry = Some(testOverseasTaxIdentifierCountry),
          optUtr = Some(Sautr(testSautr))
        )(messages, mockAppConfig)

        actualSummaryList mustBe Seq(
          testUtrRow(changeValuePageLink = overseasControllers.routes.CaptureUtrController.show(testJourneyId)),
          testOverseasTaxIdentifierRow,
          testOverseasTaxIdentifierCountryRow
        )

      }
      "the user could entered utr and overseas tax identifier buy they did not" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optOverseasTaxIdentifier = None,
          optOverseasTaxIdentifiersCountry = None,
          optUtr = None
        )(messages, mockAppConfig)

        actualSummaryList mustBe Seq(
          testNoUtrRow("The business does not have a UTR", changeValuePageLink = overseasControllers.routes.CaptureUtrController.show(testJourneyId)),
          testNoOverseasTaxIdentifiersRow
        )

      }
    }

    "raise an internal server exception" when {
      "the overseas tax identifier is defined, but the associated country is not" in {
        intercept[InternalServerException](
          rowBuilderUnderTest.buildSummaryListRows(
            journeyId = testJourneyId,
            optOverseasTaxIdentifier = Some(testOverseasTaxIdentifier),
            optOverseasTaxIdentifiersCountry = None,
            optUtr = Some(Sautr(testSautr))
          )(messages, mockAppConfig)
        )
      }
    }

    "build a summary list sequence containing cy translation" when {
      "there is a cookie specifying cy language" in {
        val incomingRequest = FakeRequest().withCookies(Cookie("PLAY_LANG","cy"))
        val messagesInWelsh: Messages = app.injector.instanceOf[MessagesApi].preferred(incomingRequest)

        val actualSummaryList: Seq[SummaryListRow] = rowBuilderUnderTest.buildSummaryListRows(
          journeyId = testJourneyId,
          optOverseasTaxIdentifier = Some(testOverseasTaxIdentifier),
          optOverseasTaxIdentifiersCountry = Some("AF"),
          optUtr = Some(Sautr(testSautr))
        )(messagesInWelsh, mockAppConfig)

        actualSummaryList.size must  be(3)

        actualSummaryList(1) must be(
          SummaryListRow(
            key = Key(content = Text("Dynodydd treth tramor")),
            value = Value(HtmlContent(s"Iawn, $testOverseasTaxIdentifier")),
            actions = Some(Actions(items = Seq(
              ActionItem(
                href = overseasControllers.routes.CaptureOverseasTaxIdentifierController.show(testJourneyId).url,
                content = Text("Newid"),
                visuallyHiddenText = Some("Dynodydd treth tramor")
              )
            )))
          )
        )

        actualSummaryList(2) must be(
          SummaryListRow(
            key = Key(content = Text("Y wlad a gyhoeddodd y dynodydd treth tramor")),
            value = Value(HtmlContent("Affganistan")),
            actions = Some(Actions(items = Seq(
              ActionItem(
                href = overseasControllers.routes.CaptureOverseasTaxIdentifiersCountryController.show(testJourneyId).url,
                content = Text("Newid"),
                visuallyHiddenText = Some("Y wlad a gyhoeddodd y dynodydd treth tramor")
              )
            )))
          )
        )

      }
    }

  }
}
