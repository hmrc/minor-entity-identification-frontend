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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.http.Status.{CREATED, FORBIDDEN, NOT_FOUND}
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.Call
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.BusinessEntity
import uk.gov.hmrc.minorentityidentificationfrontend.models.{BusinessEntity, JourneyConfig}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateBusinessVerificationJourneyConnector @Inject()(http: HttpClient,
                                                           appConfig: AppConfig
                                                          )(implicit ec: ExecutionContext) {

  def createBusinessVerificationJourney(journeyId: String,
                                        utr: String,
                                        journeyConfig: JourneyConfig
                                       )(implicit hc: HeaderCarrier): Future[BusinessVerificationJourneyCreationResponse] = {

    val (identifierJsonKey, continueUrlJsonValue) = jsonPartsBy(businessEntity = journeyConfig.businessEntity, journeyId = journeyId)

    val jsonBody: JsObject =
      Json.obj(
        "journeyType" -> "BUSINESS_VERIFICATION",
        "origin" -> journeyConfig.regime,
        "identifiers" -> Json.arr(
          Json.obj(identifierJsonKey -> utr)
        ),
        "continueUrl" -> continueUrlJsonValue.url,
        "accessibilityStatementUrl" -> journeyConfig.pageConfig.accessibilityUrl
      )

    http.POST[JsObject, BusinessVerificationJourneyCreationResponse](appConfig.createBusinessVerificationJourneyUrl, jsonBody)(
      implicitly[Writes[JsObject]],
      BusinessVerificationHttpReads,
      hc,
      ec
    )
  }

  private def jsonPartsBy(businessEntity: BusinessEntity, journeyId: String): (String, Call) = businessEntity match {
    case BusinessEntity.Trusts                    => ("saUtr", trustControllers.routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId))
    case BusinessEntity.UnincorporatedAssociation => ("ctUtr", uaControllers.routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId))
    case BusinessEntity.OverseasCompany           => throw new IllegalArgumentException("Only Trusts and UnincorporatedAssociation business entities are supported.")
  }

}

object CreateBusinessVerificationJourneyConnector {

  type BusinessVerificationJourneyCreationResponse = Either[BusinessVerificationJourneyCreationFailure, BusinessVerificationJourneyCreated]

  sealed trait BusinessVerificationJourneyCreationFailure

  case class BusinessVerificationJourneyCreated(redirectUri: String)

  case object NotEnoughEvidence extends BusinessVerificationJourneyCreationFailure

  case object UserLockedOut extends BusinessVerificationJourneyCreationFailure

  implicit object BusinessVerificationHttpReads extends HttpReads[BusinessVerificationJourneyCreationResponse] {
    override def read(method: String, url: String, response: HttpResponse): BusinessVerificationJourneyCreationResponse = {
      response.status match {
        case CREATED   =>
          (response.json \ "redirectUri").asOpt[String] match {
            case Some(redirectUri) =>
              Right(BusinessVerificationJourneyCreated(redirectUri))
            case _                 =>
              throw new InternalServerException(s"Business Verification API returned malformed JSON")
          }
        case NOT_FOUND =>
          Left(NotEnoughEvidence)
        case FORBIDDEN =>
          Left(UserLockedOut)
        case status    =>
          throw new InternalServerException(s"Business Verification API failed with status: $status")
      }
    }
  }

}
