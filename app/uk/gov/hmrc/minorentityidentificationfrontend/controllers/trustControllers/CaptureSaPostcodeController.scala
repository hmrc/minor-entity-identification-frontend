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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers

import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.forms.trustForms.TrustCaptureSaPostcodeForm
import uk.gov.hmrc.minorentityidentificationfrontend.services.{JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.trustViews.capture_sa_postcode_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureSaPostcodeController @Inject()(mcc: MessagesControllerComponents,
                                            view: capture_sa_postcode_page,
                                            journeyService: JourneyService,
                                            storageService: StorageService,
                                            val authConnector: AuthConnector,
                                            messagesHelper: MessagesHelper
                                           )(implicit val config: AppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
          case Some(authInternalId) =>
            if(isEnabled(EnableFullTrustJourney)) {
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
              journeyConfig =>
                implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                Ok(view(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CaptureSaPostcodeController.submit(journeyId),
                  form = TrustCaptureSaPostcodeForm.form
                ))
            }
        }  else throw new InternalServerException("Trust journey is not enabled")
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if(isEnabled(EnableFullTrustJourney)) {
            TrustCaptureSaPostcodeForm.form.bindFromRequest().fold(
              formWithErrors =>
                journeyService.getJourneyConfig(journeyId, authInternalId).map {
                  journeyConfig =>
                    implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                    BadRequest(view(
                      journeyId = journeyId,
                      pageConfig = journeyConfig.pageConfig,
                      formAction = routes.CaptureSaPostcodeController.submit(journeyId),
                      form = formWithErrors
                    ))
                },
              postcode =>
                storageService.storePostcode(journeyId, postcode).map {
                  _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
                }
            )
          } else throw new InternalServerException("Trust journey is not enabled")
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def noSaPostcode(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      if(isEnabled(EnableFullTrustJourney)) {
        authorised() {
          storageService.removePostcode(journeyId).map {
            _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
          }
        }
      }  else throw new InternalServerException("Trust journey is not enabled")
  }
}
