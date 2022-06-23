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
import uk.gov.hmrc.minorentityidentificationfrontend.forms.trustForms.TrustCaptureUtrForm
import uk.gov.hmrc.minorentityidentificationfrontend.services.{JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.trustViews.capture_sa_utr_trust_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureSautrController @Inject()(val authConnector: AuthConnector,
                                       journeyService: JourneyService,
                                       storageService: StorageService,
                                       mcc: MessagesControllerComponents,
                                       trustView: capture_sa_utr_trust_page,
                                       messagesHelper: MessagesHelper
                                    )(implicit val config: AppConfig,
                                      executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if(isEnabled(EnableFullTrustJourney)) {
            journeyService.getJourneyConfig(journeyId, authInternalId).map {
              journeyConfig =>
                implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                Ok(trustView(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CaptureSautrController.submit(journeyId),
                  form = TrustCaptureUtrForm.trustForm
                ))
            }
          } else throw new InternalServerException("Trust journey is not enabled")
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if(isEnabled(EnableFullTrustJourney)) {
            TrustCaptureUtrForm.trustForm.bindFromRequest().fold(
              formWithErrors =>
                journeyService.getJourneyConfig(journeyId, authInternalId).map {
                  journeyConfig =>
                    implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                    BadRequest(trustView(
                      journeyId = journeyId,
                      pageConfig = journeyConfig.pageConfig,
                      formAction = routes.CaptureSautrController.submit(journeyId),
                      form = formWithErrors
                    ))
                },
              utr =>
                for {
                _ <- storageService.storeUtr(journeyId, utr)
                  _ <- storageService.removeCHRN(journeyId)
              } yield Redirect(routes.CaptureSaPostcodeController.show(journeyId))

            )
          } else throw new InternalServerException("Trust journey is not enabled")
        case None => throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def noUtr(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        if(isEnabled(EnableFullTrustJourney)) {
          for {
            _ <- storageService.removeUtr(journeyId)
            _ <- storageService.removePostcode(journeyId)
          } yield
            Redirect(routes.CaptureCHRNController.show(journeyId))
        } else throw new InternalServerException("Trust journey is not enabled")
      }
  }

}
