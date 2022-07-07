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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthorisedFunctions
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.services._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

abstract class BaseBusinessVerificationController(mcc: MessagesControllerComponents,
                                                  businessVerificationService: BusinessVerificationService,
                                                  journeyService: JourneyService,
                                                  storageService: StorageService,
                                                  registrationOrchestrationService: RegistrationOrchestrationService
                                                 )(implicit val executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def retrieveBusinessVerificationResult(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          req.getQueryString("journeyId") match {
            case Some(businessVerificationJourneyId) =>
              for {
                journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
                verificationStatus <- businessVerificationService.retrieveBusinessVerificationStatus(businessVerificationJourneyId, journeyId)
                _ <- storageService.storeBusinessVerificationStatus(journeyId, verificationStatus)
                optUtr <- storageService.retrieveUtr(journeyId)
                _ <- registrationOrchestrationService.register(journeyId, optUtr.map(_.value), journeyConfig)
              } yield
                SeeOther(journeyConfig.fullContinueUrl(journeyId))
            case None =>
              throw new InternalServerException("Missing JourneyID from Business Verification callback")
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }
}