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

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Overseas, Utr}

import javax.inject.{Inject, Singleton}

@Singleton
class CheckYourAnswersRowBuilder @Inject()() {

  def buildSummaryListRows(journeyId: String,
                           optOverseasTaxId: Option[Overseas],
                           optUtr: Option[Utr])(implicit messages: Messages, config: AppConfig): Seq[SummaryListRow] = {

    val utrRow: Aliases.SummaryListRow = buildSummaryRow(
      messages("check-your-answers.utr"),
      optUtr match {
        case Some(utr) => utr.value
        case None => messages("check-your-answers.no_utr")
      },
      routes.CaptureUtrController.show(journeyId),
      messages
    )

    val overseasTaxIdentifiersRow: Aliases.SummaryListRow = buildSummaryRow(
      messages("check-your-answers.tax_identifiers"),
      optOverseasTaxId match {
        case Some(overseasTaxId) => Seq(overseasTaxId.taxIdentifier, config.getCountryName(overseasTaxId.country)).mkString("<br>")
        case None => messages("check-your-answers.no_tax-identifiers")
      },
      routes.CaptureOverseasTaxIdentifiersController.show(journeyId),
      messages
    )

    Seq(utrRow, overseasTaxIdentifiersRow)

  }

  private def buildSummaryRow(key: String, value: String, changeLink: Call, messages: Messages): SummaryListRow = SummaryListRow(
    key = Key(content = Text(key)),
    value = Value(HtmlContent(value)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = changeLink.url,
        content = Text(messages("base.change")),
        visuallyHiddenText = Some(key)
      )
    )))
  )
}


