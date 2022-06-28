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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers.errorControllers.{routes => errorRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullUAJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.services.{JourneyService, StorageService, SubmissionService, UAMatchingResultCalculator}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.helpers.UaCheckYourAnswersRowBuilder
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.check_your_answers_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(val authConnector: AuthConnector,
                                           journeyService: JourneyService,
                                           storageService: StorageService,
                                           submissionService: SubmissionService,
                                           uaMatchingResultCalculator: UAMatchingResultCalculator,
                                           rowBuilder: UaCheckYourAnswersRowBuilder,
                                           mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           messagesHelper: MessagesHelper
                                          )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if (isEnabled(EnableFullUAJourney)) {
            for {
              journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
              optUtr <- storageService.retrieveUtr(journeyId)
              optOfficePostcode <- storageService.retrievePostcode(journeyId)
              optCHRN <- storageService.retrieveCHRN(journeyId)
              summaryRows = rowBuilder.buildSummaryListRows(journeyId, optUtr, optOfficePostcode, optCHRN)
            } yield {
              implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
              Ok(view(
                pageConfig = journeyConfig.pageConfig,
                formAction = routes.CheckYourAnswersController.submit(journeyId),
                summaryRows = summaryRows
              ))
            }
          } else throw new InternalServerException("Unincorporated Associated journey is not enabled")
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if (isEnabled(EnableFullUAJourney)) {
            for {
              journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
              nextUrl <- submissionService.submit(
                journeyId = journeyId,
                journeyConfig = journeyConfig,
                matchingResultCalculator = uaMatchingResultCalculator,
                cannotConfirmErrorPageUrl = errorRoutes.CannotConfirmBusinessController.show(journeyId).url
              )
            } yield Redirect(nextUrl)
          } else
            throw new InternalServerException("Unincorporated Associated journey is not enabled")
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }
}
