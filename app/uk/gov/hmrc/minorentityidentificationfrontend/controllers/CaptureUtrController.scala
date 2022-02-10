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

import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.forms.CaptureUtrForm
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity
import uk.gov.hmrc.minorentityidentificationfrontend.services.{JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.{capture_utr_page, capture_utr_trust_page}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureUtrController @Inject()(val authConnector: AuthConnector,
                                     journeyService: JourneyService,
                                     storageService: StorageService,
                                     mcc: MessagesControllerComponents,
                                     view: capture_utr_page,
                                     trustView: capture_utr_trust_page
                                    )(implicit val config: AppConfig,
                                      executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig =>
              journeyConfig.businessEntity match {
                case BusinessEntity.OverseasCompany => Ok(view(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CaptureUtrController.submit(journeyId),
                  form = CaptureUtrForm.form
                ))
                case BusinessEntity.Trusts => Ok(trustView(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CaptureUtrController.submit(journeyId),
                  form = CaptureUtrForm.trustForm
                ))
                case _ => throw new InternalServerException("Business entity not found")
              }
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          CaptureUtrForm.form.bindFromRequest().fold(
            formWithErrors =>
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
                journeyConfig =>
                  journeyConfig.businessEntity match {
                    case BusinessEntity.OverseasCompany => BadRequest(view(
                      journeyId = journeyId,
                      pageConfig = journeyConfig.pageConfig,
                      formAction = routes.CaptureUtrController.submit(journeyId),
                      form = formWithErrors
                    ))
                    case BusinessEntity.Trusts => BadRequest(trustView(
                      journeyId = journeyId,
                      pageConfig = journeyConfig.pageConfig,
                      formAction = routes.CaptureUtrController.submit(journeyId),
                      form = formWithErrors
                    ))
                    case BusinessEntity.UnincorporatedAssociation => throw new InternalServerException("Business entity not found")
                  }
              },
            utr =>
              storageService.storeUtr(journeyId, utr).map {
                _ => Redirect(routes.CaptureOverseasTaxIdentifiersController.show(journeyId))
              }
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def noUtr(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        storageService.removeUtr(journeyId).map {
          _ => Redirect(routes.CaptureOverseasTaxIdentifiersController.show(journeyId))
        }
      }
  }

}
