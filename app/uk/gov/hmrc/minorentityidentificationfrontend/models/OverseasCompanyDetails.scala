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

package uk.gov.hmrc.minorentityidentificationfrontend.models

import play.api.libs.json._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessVerificationStatus.writeForJourneyContinuation
import uk.gov.hmrc.minorentityidentificationfrontend.models.RegistrationStatus.{format => regFormat}

case class OverseasCompanyDetails(optUtr: Option[Utr],
                                  optOverseas: Option[Overseas])

object OverseasCompanyDetails {
  implicit val format: OFormat[OverseasCompanyDetails] = new OFormat[OverseasCompanyDetails] {
    def writes(overseasCompanyDetails: OverseasCompanyDetails): JsObject = {
      val utrBlock: JsObject = overseasCompanyDetails.optUtr match {
        case Some(utr) => Json.obj(utr.utrType -> utr.value)
        case None => Json.obj()
      }

      val overseasTaxIdentifiersBlock: JsObject = overseasCompanyDetails.optOverseas match {
        case Some(overseasTaxIdentifiers) => Json.obj(
          "overseas" -> Json.obj(
            "taxIdentifier" -> overseasTaxIdentifiers.taxIdentifier,
            "country" -> overseasTaxIdentifiers.country
          ))
        case None => Json.obj()
      }

      Json.obj(
        "identifiersMatch" -> false,
        "businessVerification" -> Json.toJson(Json.toJson(writeForJourneyContinuation(BusinessVerificationNotEnoughInformationToChallenge))),
        "registration" -> Json.toJson(RegistrationNotCalled)(regFormat.writes)
      ) ++ utrBlock ++ overseasTaxIdentifiersBlock
    }

    def reads(json: JsValue): JsResult[OverseasCompanyDetails] = {
      for {
        optUtr <- (json \ "utr" \ "value").validateOpt[String]
        optUtrType <- (json \ "utr" \ "type").validateOpt[String]
        optPostcode <- (json \ "overseas").validateOpt[Overseas]
      } yield {
        val utr = optUtrType match {
          case Some("sautr") if optUtr.isDefined => Some(Sautr(optUtr.get))
          case Some("ctutr") if optUtr.isDefined => Some(Ctutr(optUtr.get))
          case _ => None
        }
        OverseasCompanyDetails(utr, optPostcode)
      }
    }
  }

  def writesForAudit(optOverseasCompanyDetails: Option[OverseasCompanyDetails], businessVerificationCheck: Boolean): JsObject = {
    val unMatchable = "unmatchable"
    optOverseasCompanyDetails match {
      case Some(overseasDetails) =>
        val optUtrBlock = overseasDetails.optUtr match {
          case Some(utr: Ctutr) => Json.obj("userCTUTR" -> utr.value, "isMatch" -> unMatchable)
          case Some(utr: Sautr) => Json.obj("userSAUTR" -> utr.value, "isMatch" -> unMatchable)
          case None => Json.obj("isMatch" -> unMatchable)
        }
        val overseasIdentifiersBlock = overseasDetails.optOverseas match {
          case Some(overseas) => Json.obj(
            "overseasTaxIdentifier" -> overseas.taxIdentifier,
            "overseasTaxIdentifierCountry" -> overseas.country)
          case _ => Json.obj()
        }

        Json.obj(
          "VerificationStatus" -> checkVerificationStatus(businessVerificationCheck),
          "RegisterApiStatus" -> "not called"
        ) ++ optUtrBlock ++ overseasIdentifiersBlock
      case None =>
        Json.obj(
          "VerificationStatus" -> checkVerificationStatus(businessVerificationCheck),
          "RegisterApiStatus" -> "not called",
          "isMatch" -> unMatchable
        )
    }
  }

  private def checkVerificationStatus(businessVerificationCheck: Boolean): String = {
    if (businessVerificationCheck) {
      "Not Enough Information to call BV"
    } else {
      "not requested"
    }
  }

}
