/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.services.{CheckYourAnswersRowBuilder, JourneyService, StorageService}
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.check_your_answers_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(val authConnector: AuthConnector,
                                           journeyService: JourneyService,
                                           storageService: StorageService,
                                           rowBuilder: CheckYourAnswersRowBuilder,
                                           mcc: MessagesControllerComponents,
                                           view: check_your_answers_page
                                          )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        for {
          journeyConfig <- journeyService.getJourneyConfig(journeyId)
          utr <- storageService.retrieveUtr(journeyId)
          summaryRows = rowBuilder.buildSummaryListRows(journeyId, utr)
        } yield Ok(view(
          pageConfig = journeyConfig.pageConfig,
          formAction = routes.CheckYourAnswersController.submit(journeyId),
          summaryRows = summaryRows
        ))
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        journeyService.getJourneyConfig(journeyId).map {
          journeyConfig => Redirect(journeyConfig.continueUrl + s"?journeyId=$journeyId")
        }
      }
  }

}
