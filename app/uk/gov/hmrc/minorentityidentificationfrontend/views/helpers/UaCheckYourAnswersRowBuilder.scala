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
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers
import uk.gov.hmrc.minorentityidentificationfrontend.models.Utr

import javax.inject.Singleton

@Singleton
class UaCheckYourAnswersRowBuilder() {

  def buildSummaryListRows(journeyId: String,
                           optUtr: Option[Utr],
                           optOfficePostcode: Option[String],
                           optCHRN: Option[String]
                          )(implicit messages: Messages): Seq[SummaryListRow] = {

    val utrRow: Aliases.SummaryListRow = CheckYourAnswersRowBuilder.utrSummaryRow(
      optUtr = optUtr,
      noUtrMessageKey = "check-your-answers.no_ua_utr",
      changeValuePageLink = uaControllers.routes.CaptureCtutrController.show(journeyId),
      messages = messages
    )

    val charityHMRCReferenceNumberRow: Aliases.SummaryListRow = CheckYourAnswersRowBuilder.charityHMRCReferenceNumberRow(
      optCHRN,
      "check-your-answers.no_ua_charity_hmrc_reference_number",
      uaControllers.routes.CaptureCHRNController.show(journeyId),
      messages
    )

    def officePostcodeRow(): Aliases.SummaryListRow = CheckYourAnswersRowBuilder.buildSummaryRow(
      messages(checkYourAnswersMessageKey(keySuffix = "office_postcode")),
      optOfficePostcode match {
        case Some(postcode) => postcode
        case None => throw new InternalServerException("Office post code is not available")
      },
      changeValuePageLink = uaControllers.routes.CaptureOfficePostcodeController.show(journeyId),
      messages = messages
    )

    (optUtr, optOfficePostcode, optCHRN) match {
      case (Some(_), Some(_), None) => Seq(utrRow, officePostcodeRow())
      case (None, None, _) => Seq(utrRow, charityHMRCReferenceNumberRow)
      case (Some(_), _, Some(_)) => throw new IllegalStateException("User cannot have CTUTR and charity HMRC reference number at the same time")
      case (None, Some(_), _) => throw new IllegalStateException("User cannot have registered office postcode when they don't have CTUTR")
    }

  }

  private def checkYourAnswersMessageKey(keySuffix: String): String = s"check-your-answers.$keySuffix"

}
