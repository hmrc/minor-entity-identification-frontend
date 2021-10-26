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

package uk.gov.hmrc.minorentityidentificationfrontend.testonly.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.OverseasCompany
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.connectors.TestCreateJourneyConnector
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.forms.TestCreateJourneyForm
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.views.html.test_create_journey
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestCreateOverseasCompanyJourneyController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                                           testCreateJourneyConnector: TestCreateJourneyConnector,
                                                           view: test_create_journey,
                                                           val authConnector: AuthConnector
                                                          )(implicit ec: ExecutionContext,
                                                            appConfig: AppConfig) extends FrontendController(messagesControllerComponents) with AuthorisedFunctions {


  private val defaultPageConfig = PageConfig(
    optServiceName = None,
    deskProServiceId = "vrs",
    signOutUrl = appConfig.vatRegFeedbackUrl,
    accessibilityUrl = appConfig.vatRegAccessibilityStatementUrl
  )

  private val defaultJourneyConfig = JourneyConfig(
    continueUrl = s"${appConfig.selfUrl}/identify-your-overseas-business/test-only/retrieve-journey",
    pageConfig = defaultPageConfig,
    businessEntity = OverseasCompany
  )

  val show: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(
          Ok(view(defaultPageConfig, TestCreateJourneyForm.form(OverseasCompany).fill(defaultJourneyConfig), routes.TestCreateOverseasCompanyJourneyController.submit()))
        )
      }
  }

  val submit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        TestCreateJourneyForm.form(OverseasCompany).bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(defaultPageConfig, formWithErrors, routes.TestCreateOverseasCompanyJourneyController.submit()))
            ),
          journeyConfig =>
            testCreateJourneyConnector.createOverseasCompanyJourney(journeyConfig).map {
              journeyUrl => SeeOther(journeyUrl)
            }
        )
      }
  }
}