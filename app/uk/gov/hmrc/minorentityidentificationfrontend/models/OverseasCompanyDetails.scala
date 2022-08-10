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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessVerificationStatus.writeForJourneyContinuation
import uk.gov.hmrc.minorentityidentificationfrontend.models.RegistrationStatus.{format => regFormat}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WritesForJourneyEnd

case class OverseasCompanyDetails(optUtr: Option[Utr],
                                  optOverseasTaxIdentifier: Option[String],
                                  optOverseasTaxIdentifierCountry: Option[String])

object OverseasCompanyDetails {
  implicit val format: OFormat[OverseasCompanyDetails] = new OFormat[OverseasCompanyDetails] {
    def writes(overseasCompanyDetails: OverseasCompanyDetails): JsObject = {
      val utrBlock: JsObject = overseasCompanyDetails.optUtr match {
        case Some(utr) => Json.obj(utr.utrType -> utr.value)
        case None => Json.obj()
      }

      val overseasTaxIdentifierBlock: JsObject = overseasCompanyDetails.optOverseasTaxIdentifier match {
        case Some(overseasTaxIdentifier) => Json.obj("overseasTaxIdentifier" -> overseasTaxIdentifier)
        case None => Json.obj()
      }

      val overseasTaxIdentifierCountryBlock: JsObject = overseasCompanyDetails.optOverseasTaxIdentifierCountry match {
        case Some(overseasTaxIdentifierCountry) => Json.obj("country" -> overseasTaxIdentifierCountry)
        case None => Json.obj()
      }

      Json.obj(
        "identifiersMatch" -> false,
        "businessVerification" -> Json.toJson(Json.toJson(writeForJourneyContinuation(BusinessVerificationNotEnoughInformationToChallenge))),
        "registration" -> Json.toJson(RegistrationNotCalled)(regFormat.writes)
      ) ++ utrBlock ++ overseasTaxIdentifierBlock ++ overseasTaxIdentifierCountryBlock
    }

    def reads(json: JsValue): JsResult[OverseasCompanyDetails] = {
      for {
        optUtr <- (json \ "utr" \ "value").validateOpt[String]
        optUtrType <- (json \ "utr" \ "type").validateOpt[String]
        optOverseasTaxIdentifier <- (json \ "overseasTaxIdentifier").validateOpt[String]
        optOverseasTaxIdentifierCountry <- (json \ "country").validateOpt[String]
        optOverseas <- (json \ "overseas").validateOpt[Overseas]
      } yield {
        val utr = optUtrType match {
          case Some("sautr") if optUtr.isDefined => Some(Sautr(optUtr.get))
          case Some("ctutr") if optUtr.isDefined => Some(Ctutr(optUtr.get))
          case _ => None
        }
        val (overseasTaxIdentifier, overseasTaxIdentifierCountry) = determineOverseasTaxIdentifierDetails(
          optOverseasTaxIdentifier,
          optOverseasTaxIdentifierCountry,
          optOverseas
        )
        OverseasCompanyDetails(utr, overseasTaxIdentifier, overseasTaxIdentifierCountry)
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
        val overseasIdentifiersBlock = (overseasDetails.optOverseasTaxIdentifier, overseasDetails.optOverseasTaxIdentifierCountry) match {
          case (Some(taxIdentifier), Some(country)) => Json.obj(
            "overseasTaxIdentifier" -> taxIdentifier,
            "overseasTaxIdentifierCountry" -> country)
          case (None, None) => Json.obj()
          case _ => throw new InternalServerException("Error: Unexpected combination of tax identifier and country for an overseas business journey")
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

  def writesForJourneyEnd(overseasCompanyDetails: OverseasCompanyDetails, journeyConfig: JourneyConfig): JsObject = {

    val utrBlock: JsObject = overseasCompanyDetails.optUtr match {
      case Some(utr) => Json.obj(utr.utrType -> utr.value)
      case None => Json.obj()
    }

    val overseasBlock: JsObject = (overseasCompanyDetails.optOverseasTaxIdentifier, overseasCompanyDetails.optOverseasTaxIdentifierCountry) match {
      case (Some(identifier), Some(country)) => Json.obj(
        "overseas" -> Json.obj(
          "taxIdentifier" -> identifier,
          "country" -> country
        )
      )
      case (None, None) => Json.obj()
      case _ => throw new InternalServerException("Error: Unexpected combination of tax identifier and country for an overseas business journey")
    }

    val businessVerificationBlock: JsObject = WritesForJourneyEnd.businessVerificationBlock(
      Some(BusinessVerificationNotEnoughInformationToChallenge),
      journeyConfig.businessVerificationCheck
    )

    Json.obj(
      "identifiersMatch" -> false,
      "registration" -> Json.toJson(RegistrationNotCalled)(regFormat.writes)
    ) ++ utrBlock ++ overseasBlock ++ businessVerificationBlock
  }

  private def determineOverseasTaxIdentifierDetails(optOverseasTaxIdentifier: Option[String],
                                                   optOverseasTaxIdentifierCountry: Option[String],
                                                   optOverseas: Option[Overseas]): (Option[String], Option[String]) =
    (optOverseasTaxIdentifier, optOverseasTaxIdentifierCountry) match {
      case (Some(identifier), Some(country)) => (Some(identifier), Some(country))
      case (None, None) => optOverseas match { // TODO - Remove after code has been running for a while
        case Some(overseas) => (Some(overseas.taxIdentifier), Some(overseas.country))
        case None => (None, None)
      }
      case _ => throw new InternalServerException("Error: Unexpected combination of tax identifier and country for an overseas business journey")
    }


  private def checkVerificationStatus(businessVerificationCheck: Boolean): String = {
    if (businessVerificationCheck) {
      "Not Enough Information to call BV"
    } else {
      "not requested"
    }
  }

}
