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

import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config._
import uk.gov.hmrc.minorentityidentificationfrontend.forms.uaForms.UaCaptureUtrForm
import uk.gov.hmrc.minorentityidentificationfrontend.services.{JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.uaViews.capture_ct_utr_ua_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureCtutrController @Inject()(val authConnector: AuthConnector,
                                       journeyService: JourneyService,
                                       storageService: StorageService,
                                       mcc: MessagesControllerComponents,
                                       uaView: capture_ct_utr_ua_page
                                      )(implicit val config: AppConfig,
                                        executionContext: ExecutionContext)
  extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if (isEnabled(EnableFullUAJourney)) {
            journeyService.getJourneyConfig(journeyId, authInternalId).map {
              journeyConfig =>
                Ok(uaView(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CaptureCtutrController.submit(journeyId),
                  form = UaCaptureUtrForm.uaForm
                ))
            }
          } else throw new InternalServerException("Unincorporated association journey is not enabled")
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if (isEnabled(EnableFullUAJourney)) {
            UaCaptureUtrForm.uaForm.bindFromRequest().fold(
              formWithErrors =>
                journeyService.getJourneyConfig(journeyId, authInternalId).map {
                  journeyConfig =>
                    BadRequest(uaView(
                      journeyId = journeyId,
                      pageConfig = journeyConfig.pageConfig,
                      formAction = routes.CaptureCtutrController.submit(journeyId),
                      form = formWithErrors
                    ))
                },
              utr =>
                for {
                  _ <- storageService.storeUtr(journeyId, utr)
                  _ <- storageService.removeCHRN(journeyId)
                } yield Redirect(routes.CaptureOfficePostcodeController.show(journeyId))
            )
          } else throw new InternalServerException("Unincorporated association journey is not enabled")
        case None => throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def noUtr(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        if (isEnabled(EnableFullUAJourney)) {
          for {
            _ <- storageService.removeUtr(journeyId)
            _ <- storageService.removeSaPostcode(journeyId)
          } yield Redirect(routes.CaptureCHRNController.show(journeyId))
        } else throw new InternalServerException("Unincorporated association journey is not enabled")
      }
  }

}
