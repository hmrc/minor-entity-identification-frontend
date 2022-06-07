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
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Overseas, Utr}

import javax.inject.Singleton

@Singleton
class OverseasCheckYourAnswersRowBuilder() {

  def buildSummaryListRows(journeyId: String,
                           optOverseasTaxId: Option[Overseas],
                           optUtr: Option[Utr])(implicit messages: Messages, config: AppConfig): Seq[SummaryListRow] = {

    val utrRow: Aliases.SummaryListRow = CheckYourAnswersRowBuilder.utrSummaryRow(
      optUtr = optUtr,
      changeValuePageLink = overseasControllers.routes.CaptureUtrController.show(journeyId),
      messages = messages
    )

    val overseasTaxIdentifiersRow: Aliases.SummaryListRow = CheckYourAnswersRowBuilder.buildSummaryRow(
      messages("check-your-answers.tax_identifiers"),
      optOverseasTaxId match {
        case Some(overseasTaxId) => Seq(overseasTaxId.taxIdentifier, config.getCountryName(overseasTaxId.country, messages.lang.code)).mkString("<br>")
        case None                => messages("check-your-answers.no_tax-identifiers")
      },
      changeValuePageLink = overseasControllers.routes.CaptureOverseasTaxIdentifiersController.show(journeyId),
      messages = messages
    )

    Seq(utrRow, overseasTaxIdentifiersRow)

  }

}


