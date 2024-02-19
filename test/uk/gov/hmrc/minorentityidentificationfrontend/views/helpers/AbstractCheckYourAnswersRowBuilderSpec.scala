/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.testSautr

abstract class AbstractCheckYourAnswersRowBuilderSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  def testUtrRow(changeValuePageLink: Call): SummaryListRow = SummaryListRow(
    key = Key(content = Text("Unique Taxpayer Reference (UTR)")),
    value = Value(content = HtmlContent(testSautr)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = changeValuePageLink.url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique Taxpayer Reference (UTR)")
      )
    )))
  )

  def testNoUtrRow(noUtrMessage: String, changeValuePageLink: Call): SummaryListRow = SummaryListRow(
    key = Key(content = Text("Unique Taxpayer Reference (UTR)")),
    value = Value(content = HtmlContent(noUtrMessage)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = changeValuePageLink.url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique Taxpayer Reference (UTR)")
      )
    )))
  )

}
