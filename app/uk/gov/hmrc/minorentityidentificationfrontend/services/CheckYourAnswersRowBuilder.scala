/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.minorentityidentificationfrontend.services

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.minorentityidentificationfrontend.models.Utr

import javax.inject.{Inject, Singleton}

@Singleton
class CheckYourAnswersRowBuilder @Inject()() {

  def buildSummaryListRows(journeyId: String, optUtr: Option[Utr])(implicit messages: Messages): Seq[SummaryListRow] = {

    val utrRow =
      buildSummaryRow(
        messages("check-your-answers.utr"),
        optUtr match {
          case Some(utr) => utr.value
          case None => messages("check-your-answers.no_utr")
        },
        routes.CaptureUtrController.show(journeyId)
      )

    Seq(utrRow)

  }

  private def buildSummaryRow(key: String, value: String, changeLink: Call) = SummaryListRow(
    key = Key(content = Text(key)),
    value = Value(HtmlContent(value)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = changeLink.url,
        content = Text("Change"),
        visuallyHiddenText = Some(key)
      )
    )))
  )
}


