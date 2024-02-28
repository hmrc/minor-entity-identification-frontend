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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers.{routes => overseasControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.forms.overseasForm.OverseasCaptureTaxIdentifierForm
import uk.gov.hmrc.minorentityidentificationfrontend.services.{JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.overseasCompanyViews.capture_overseas_tax_identifier_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CaptureOverseasTaxIdentifierController @Inject()(mcc: MessagesControllerComponents,
                                                       journeyService: JourneyService,
                                                       storageService: StorageService,
                                                       view: capture_overseas_tax_identifier_page,
                                                       messagesHelper: MessagesHelper,
                                                       val authConnector: AuthConnector
                                                       )(implicit val config: AppConfig,
                                                         executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig =>
                implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                Ok(view(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = overseasControllerRoutes.CaptureOverseasTaxIdentifierController.submit(journeyId),
                  form = OverseasCaptureTaxIdentifierForm.form
                ))
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }


  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          OverseasCaptureTaxIdentifierForm.form.bindFromRequest().fold(
            formWithErrors =>
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
                journeyConfig =>
                  implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                  BadRequest(view(
                    journeyId = journeyId,
                    pageConfig = journeyConfig.pageConfig,
                    formAction = overseasControllerRoutes.CaptureOverseasTaxIdentifierController.submit(journeyId),
                    form = formWithErrors
                  ))
              },
              {
                case Some(overseasTaxIdentifier) => for {
                  _ <- storageService.storeOverseasTaxIdentifier(journeyId, overseasTaxIdentifier)
                } yield {
                  Redirect(routes.CaptureOverseasTaxIdentifiersCountryController.show(journeyId))
                }
                case None => for {
                  _ <- storageService.removeOverseasTaxIdentifier(journeyId)
                  _ <- storageService.removeOverseasTaxIdentifiersCountry(journeyId)
                } yield {
                  Redirect(routes.CheckYourAnswersController.show(journeyId))
                }
              }
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

}