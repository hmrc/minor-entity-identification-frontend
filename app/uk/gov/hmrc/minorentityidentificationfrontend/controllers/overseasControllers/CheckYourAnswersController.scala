/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.minorentityidentificationfrontend.services.{AuditService, JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.helpers.OverseasCheckYourAnswersRowBuilder
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.check_your_answers_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(val authConnector: AuthConnector,
                                           journeyService: JourneyService,
                                           storageService: StorageService,
                                           auditService: AuditService,
                                           rowBuilder: OverseasCheckYourAnswersRowBuilder,
                                           mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           messagesHelper: MessagesHelper
                                          )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>

          val journeyConfigFuture = journeyService.getJourneyConfig(journeyId, authInternalId)
          val utrFuture = storageService.retrieveUtr(journeyId)
          val overseasTaxIdentifierFuture = storageService.retrieveOverseasTaxIdentifier(journeyId)
          val overseasTaxIdentifierCountryFuture = storageService.retrieveOverseasTaxIdentifiersCountry(journeyId)

          for {
            journeyConfig <- journeyConfigFuture
            utr <- utrFuture
            overseasTaxIdentifier <- overseasTaxIdentifierFuture
            overseasTaxIdentifierCountry <- overseasTaxIdentifierCountryFuture
            summaryRows = rowBuilder.buildSummaryListRows(
              journeyId = journeyId,
              optOverseasTaxIdentifier = overseasTaxIdentifier,
              optOverseasTaxIdentifiersCountry = overseasTaxIdentifierCountry,
              optUtr = utr)
          } yield {
            implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
            Ok(view(
              pageConfig = journeyConfig.pageConfig,
              formAction = routes.CheckYourAnswersController.submit(journeyId),
              summaryRows = summaryRows
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
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig => {
              auditService.auditJourney(journeyId, authInternalId)
              Redirect(journeyConfig.fullContinueUrl(journeyId))
            }
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

}
