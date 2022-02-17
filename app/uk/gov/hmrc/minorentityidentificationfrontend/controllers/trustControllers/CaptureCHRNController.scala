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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers.{routes => overseasControllersRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustControllersRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.forms.trustForms.CaptureCHRNForm
import uk.gov.hmrc.minorentityidentificationfrontend.services.{JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.trustViews.capture_chrn_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureCHRNController @Inject()(val authConnector: AuthConnector,
                                      journeyService: JourneyService,
                                      storageService: StorageService,
                                      mcc: MessagesControllerComponents,
                                      view: capture_chrn_page)
                                     (implicit val config: AppConfig, executionContext: ExecutionContext)
                                           extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if(isEnabled(EnableFullTrustJourney)) {
            journeyService.getJourneyConfig(journeyId, authInternalId).map {
              journeyConfig => Ok(view(
                journeyId = journeyId,
                pageConfig = journeyConfig.pageConfig,
                formAction = trustControllersRoutes.CaptureCHRNController.submit(journeyId),
                form = CaptureCHRNForm.form
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
            CaptureCHRNForm.form.bindFromRequest().fold(
              formWithErrors =>
                journeyService.getJourneyConfig(journeyId, authInternalId).map {
                  journeyConfig =>
                    BadRequest(view(
                      journeyId = journeyId,
                      pageConfig = journeyConfig.pageConfig,
                      formAction = trustControllersRoutes.CaptureCHRNController.submit(journeyId),
                      form = formWithErrors
                    ))
              },
              chrn => storageService.storeCHRN(journeyId, chrn).map {
                _ => Redirect(overseasControllersRoutes.CheckYourAnswersController.show(journeyId)) // TODO Redirect to trusts CYA page
              }
            )
          } else throw new InternalServerException("Trust journey is not enabled")
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }

  }

  def noChrn(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        if (isEnabled(EnableFullTrustJourney)) {
          storageService.removeCHRN(journeyId).map {
            _ => Redirect(overseasControllersRoutes.CheckYourAnswersController.show(journeyId))
          }
        } else
          throw new InternalServerException("Trust journey is not enabled")
      }
  }

}