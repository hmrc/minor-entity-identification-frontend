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

package uk.gov.hmrc.minorentityidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json.{JsObject, JsSuccess, Json, __}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Session}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.JourneyConfigRepository
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.models.{AbroadResponse, GBResponse, KnownFactsNotFound}
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.service.TestStorageService
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.stubs.controllers.utils.JsonUtils.jsonFromFile
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustsKnownFactsVerificationStubController @Inject()(controllerComponents: ControllerComponents,
                                                           testStorageService: TestStorageService
                                                          )(implicit ec: ExecutionContext) extends BackendController(controllerComponents) {

  def stubTrustKnownFacts(journeyId: String): Action[AnyContent] = Action.async {
    //    implicit request =>
    //      request.session.get("stubs").map {
    //        case "Abroad Response" => Future.successful(Ok(jsonFromFile("resources/TrustsKnownFactsVerificationStub/AbroadResponse.json")))
    //        case "Not Found" => Future.successful(NotFound)
    //        case "GBResponse" =>
    //          val json = jsonFromFile("resources/TrustsKnownFactsVerificationStub/GBResponse.json").as[JsObject]
    //          val jsonTransformer = (__ \ 'getTrust \ 'declaration \ 'address).json.update(
    //            __.read[JsObject].map { o => o ++ Json.obj("postCode" -> "QQ1 1QQ") }
    //          )
    //          val updated = json.transform(jsonTransformer) match {
    //            case JsSuccess(value, _) => value
    //            case _ => throw new InternalServerException("Json update went wrong")
    //          }
    //          Future.successful(Ok(updated))
    //      }.getOrElse(throw new InternalServerException("Session went wrong: " + request))
    implicit request =>
      testStorageService.retrieveStubs(journeyId).flatMap {
        {
          case Some(testSetup) =>
            testSetup.knownFactsMatch match {
              case AbroadResponse => Future.successful(Ok(jsonFromFile("resources/TrustsKnownFactsVerificationStub/AbroadResponse.json")))
              case KnownFactsNotFound => Future.successful(NotFound)
              case GBResponse(postcode) =>
                val json = jsonFromFile("resources/TrustsKnownFactsVerificationStub/GBResponse.json").as[JsObject]
                val jsonTransformer = (__ \ 'getTrust \ 'declaration \ 'address).json.update(
                  __.read[JsObject].map { o => o ++ Json.obj("postCode" -> postcode) }
                )
                val updated = json.transform(jsonTransformer) match {
                  case JsSuccess(value, _) => value
                  case _ => throw new InternalServerException("Json update went wrong")
                }
                Future.successful(Ok(updated))
            }
          case None => throw new InternalServerException("No data found")
        }
      }

  }

}
