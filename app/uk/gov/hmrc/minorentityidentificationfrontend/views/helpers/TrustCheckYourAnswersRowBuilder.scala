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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers
import uk.gov.hmrc.minorentityidentificationfrontend.models.Utr

import javax.inject.Singleton

@Singleton
class TrustCheckYourAnswersRowBuilder() {

  def buildSummaryListRows(journeyId: String,
                           optUtr: Option[Utr],
                           optPostcode: Option[String],
                           optCharityHMRCReferenceNumber: Option[String]
                          )(implicit messages: Messages): Seq[SummaryListRow] = {

    val utrRow: Aliases.SummaryListRow = CheckYourAnswersRowBuilder.utrSummaryRow(
      optUtr = optUtr,
      noUtrMessageKey = "check-your-answers.no_trust_utr",
      changeValuePageLink = trustControllers.routes.CaptureSautrController.show(journeyId),
      messages = messages
    )

    val charityHMRCReferenceNumberRow: Aliases.SummaryListRow = CheckYourAnswersRowBuilder.charityHMRCReferenceNumberRow(
      optCharityHMRCReferenceNumber,
      noCHRNMessageKey = "check-your-answers.no_charity_hmrc_reference_number",
      trustControllers.routes.CaptureCHRNController.show(journeyId),
      messages
    )

    def postcodeRow(): Aliases.SummaryListRow = CheckYourAnswersRowBuilder.buildSummaryRow(
      messages(checkYourAnswersMessageKey(keySuffix = "sa_postcode")),
      optPostcode match {
        case Some(postcode) => postcode
        case None           => messages(checkYourAnswersMessageKey(keySuffix = "no_sa_postcode"))
      },
      changeValuePageLink = trustControllers.routes.CaptureSaPostcodeController.show(journeyId),
      messages = messages
    )

    (optUtr, optPostcode, optCharityHMRCReferenceNumber) match {
      case (Some(_), _, None)    => Seq(utrRow, postcodeRow())
      case (None, None, _)       => Seq(utrRow, charityHMRCReferenceNumberRow)
      case (Some(_), _, Some(_)) => throw new IllegalStateException("User cannot have SAUTR and charity HMRC reference number at the same time")
      case (None, Some(_), _)    => throw new IllegalStateException("User cannot have postcode when they dont have SAUTR")
    }

  }

  private def checkYourAnswersMessageKey(keySuffix: String): String = s"check-your-answers.$keySuffix"

}


