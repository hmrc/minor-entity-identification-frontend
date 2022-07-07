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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, InjectedController}
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.JourneyConfigRepository
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.service.TestStorageService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessVerificationStubController @Inject()(testStorageService: TestStorageService,
                                                   controllerComponents: ControllerComponents
                                                  )(implicit ec: ExecutionContext) extends BackendController(controllerComponents) {

  private val origin = "vat"
  private val businessVerificationJourneyId = UUID.randomUUID.toString

  def createBusinessVerificationJourney: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      val continueUrl: String = (request.body \ "continueUrl").as[String]

      Future.successful {
        Created(Json.obj(
          "redirectUri" -> (continueUrl + s"?journeyId=$businessVerificationJourneyId")
        ))
      }
  }

  def retrieveVerificationResult(businessVerificationJourneyId: String, journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      testStorageService.retrieveStubs(journeyId).map {
        {
          case Some(stubs) =>
            Ok(Json.obj(
              "journeyType" -> "BUSINESS_VERIFICATION",
              "origin" -> origin,
              "identifier" -> {
                "saUtr" -> "1234567890"
              },
              "verificationStatus" -> stubs.businessVerificationStub.toUpperCase
            ))
        }
      }
  }

}


