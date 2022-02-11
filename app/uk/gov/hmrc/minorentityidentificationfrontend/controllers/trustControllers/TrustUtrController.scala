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

import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.forms.trustForms.TrustCaptureUtrForm
import uk.gov.hmrc.minorentityidentificationfrontend.services.{JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.trustViews.capture_utr_trust_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TrustUtrController @Inject()(val authConnector: AuthConnector,
                                   journeyService: JourneyService,
                                   storageService: StorageService,
                                   mcc: MessagesControllerComponents,
                                   trustView: capture_utr_trust_page
                                    )(implicit val config: AppConfig,
                                      executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig => Ok(trustView(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.TrustUtrController.submit(journeyId),
                  form = TrustCaptureUtrForm.trustForm
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
          TrustCaptureUtrForm.trustForm.bindFromRequest().fold(
            formWithErrors =>
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
                journeyConfig => BadRequest(trustView(
                      journeyId = journeyId,
                      pageConfig = journeyConfig.pageConfig,
                      formAction = routes.TrustUtrController.submit(journeyId),
                      form = formWithErrors
                    ))
              },
            utr =>
              storageService.storeUtr(journeyId, utr).map {
                _ => Redirect(routes.CaptureSaPostcodeController.show(journeyId))
              }
          )
        case None => throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def noUtr(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        storageService.removeUtr(journeyId).map {
          _ => Redirect(routes.CaptureSaPostcodeController.show(journeyId))
        }
      }
  }

}
