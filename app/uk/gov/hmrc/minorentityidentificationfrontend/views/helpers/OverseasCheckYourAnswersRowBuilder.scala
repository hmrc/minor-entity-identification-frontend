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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers
import uk.gov.hmrc.minorentityidentificationfrontend.models.Utr

import javax.inject.Singleton

@Singleton
class OverseasCheckYourAnswersRowBuilder() {

  def buildSummaryListRows(journeyId: String,
                           optOverseasTaxIdentifier: Option[String],
                           optOverseasTaxIdentifiersCountry: Option[String],
                           optUtr: Option[Utr])(implicit messages: Messages, config: AppConfig): Seq[SummaryListRow] = {

    val utrRow: Aliases.SummaryListRow = CheckYourAnswersRowBuilder.utrSummaryRow(
      optUtr = optUtr,
      noUtrMessageKey = "check-your-answers.no_utr",
      changeValuePageLink = overseasControllers.routes.CaptureUtrController.show(journeyId),
      messages = messages
    )

    val overseasTaxIdentifierRows: Seq[Aliases.SummaryListRow] = (optOverseasTaxIdentifier, optOverseasTaxIdentifiersCountry) match {
      case (Some(identifier), Some(country)) => Seq(
            createOverseasTaxIdentifierRow(journeyId, identifier), createOverseasTaxIdentifierCountryRow(journeyId, country)
          )
      case (None, None) => Seq(createOverseasTaxIdentifierNotProvidedRow(journeyId))
      case _ => throw new InternalServerException("Error: Unexpected combination of tax identifier and country for an overseas business journey")
    }

    Seq(utrRow) ++ overseasTaxIdentifierRows
  }

  private def createOverseasTaxIdentifierRow(journeyId: String, overseasTaxIdentifier: String)
                                            (implicit messages: Messages): Aliases.SummaryListRow =
      buildSummaryRow(
        key = messages("check-your-answers.tax_identifier"),
        value = messages("check-your-answers.tax_identifier_yes", overseasTaxIdentifier),
        journeyId = journeyId
      )

  private def createOverseasTaxIdentifierCountryRow(journeyId: String, country: String)
                                                   (implicit messages: Messages, appConfig: AppConfig): Aliases.SummaryListRow =
    CheckYourAnswersRowBuilder.buildSummaryRow(
      key = messages("check-your-answers.tax_identifier_country"),
      value = appConfig.getCountryName(country, messages.lang.code),
      changeValuePageLink = overseasControllers.routes.CaptureOverseasTaxIdentifiersCountryController.show(journeyId),
      messages = messages
    )

  private def createOverseasTaxIdentifierNotProvidedRow(journeyId: String)(implicit messages: Messages): Aliases.SummaryListRow =
    buildSummaryRow(
      key = messages("check-your-answers.tax_identifier"),
      value = messages("app.common.no"),
      journeyId = journeyId
    )

  private def buildSummaryRow(key: String, value: String, journeyId: String)(implicit messages: Messages): Aliases.SummaryListRow =
    CheckYourAnswersRowBuilder.buildSummaryRow(
      key = key,
      value = value,
      changeValuePageLink = overseasControllers.routes.CaptureOverseasTaxIdentifierController.show(journeyId),
      messages = messages
    )

}


