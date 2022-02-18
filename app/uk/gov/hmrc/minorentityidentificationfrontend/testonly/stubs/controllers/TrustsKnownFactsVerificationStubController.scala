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

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.stubs.controllers.utils.JsonUtils.jsonFromFile
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class TrustsKnownFactsVerificationStubController @Inject()(controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  def stubTrustKnownFacts(sautr: String): Action[AnyContent] = Action.async {
    sautr match {
      case "1234567891" => Future.successful(Ok(jsonFromFile("resources/TrustsKnownFactsVerificationStub/AbroadResponse")))
      case "1234567892" => Future.successful(NotFound)
      case _ => Future.successful(Ok(jsonFromFile("resources/TrustsKnownFactsVerificationStub/GBResponse")))
    }
  }
}
