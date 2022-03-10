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

package uk.gov.hmrc.minorentityidentificationfrontend.api.controllers

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.api.controllers.JourneyController._
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers.{routes => overseasControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers.{routes => uaControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, EnableFullUAJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.minorentityidentificationfrontend.services._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.UrlHelper
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyController @Inject()(val authConnector: AuthConnector,
                                  journeyService: JourneyService,
                                  storageService: StorageService,
                                  controllerComponents: ControllerComponents,
                                  appConfig: AppConfig,
                                  auditService: AuditService,
                                  urlHelper: UrlHelper
                                 )(implicit ec: ExecutionContext)
  extends BackendController(controllerComponents) with AuthorisedFunctions with FeatureSwitching {

  def createOverseasCompanyJourney(): Action[JourneyConfig] = createJourney(OverseasCompany)

  def createTrustsJourney(): Action[JourneyConfig] = createJourney(Trusts)

  def createUnincorporatedAssociationJourney(): Action[JourneyConfig] = createJourney(UnincorporatedAssociation)

  private def createJourney(businessEntity: BusinessEntity): Action[JourneyConfig] = Action.async(parse.json[JourneyConfig] {
    json =>
      for {
        continueUrl <- (json \ continueUrlKey).validate[String]
        optServiceName <- (json \ optServiceNameKey).validateOpt[String]
        deskProServiceId <- (json \ deskProServiceIdKey).validate[String]
        signOutUrl <- (json \ signOutUrlKey).validate[String]
        accessibilityUrl <- (json \ accessibilityUrlKey).validate[String]
        businessVerificationCheck <- (json \ businessVerificationCheckKey).validateOpt[Boolean]
        regime <- (json \ regimeKey).validate[String]
      } yield JourneyConfig(continueUrl, PageConfig(optServiceName, deskProServiceId, signOutUrl, accessibilityUrl), businessEntity, businessVerificationCheck.getOrElse(true), regime)
  }) {
    implicit req =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          if (urlHelper.areRelativeOrAcceptedUrls(List(req.body.continueUrl, req.body.pageConfig.signOutUrl, req.body.pageConfig.accessibilityUrl))) {
            journeyService.createJourney(req.body, authInternalId).map(
              journeyId => {
                businessEntity match {
                  case OverseasCompany => Created(Json.obj(
                    journeyStartUrl -> s"${appConfig.selfUrl}${overseasControllerRoutes.CaptureUtrController.show(journeyId).url}"
                  ))
                  case Trusts =>
                    if (isEnabled(EnableFullTrustJourney)) {
                      val pathToRedirect = s"${appConfig.selfUrl}${trustControllerRoutes.CaptureSautrController.show(journeyId).url}"
                      Created(Json.obj(journeyStartUrl -> pathToRedirect))
                    } else {
                      auditService.auditJourney(journeyId, authInternalId)
                      Created(Json.obj(journeyStartUrl -> (req.body.continueUrl + s"?journeyId=$journeyId")))
                    }
                  case UnincorporatedAssociation =>
                    if (isEnabled(EnableFullUAJourney)) {
                      val pathToRedirect = s"${appConfig.selfUrl}${uaControllerRoutes.CaptureCtutrController.show(journeyId).url}"
                      Created(Json.obj(journeyStartUrl -> pathToRedirect))
                    } else {
                      auditService.auditJourney(journeyId, authInternalId)
                      Created(Json.obj(journeyStartUrl -> (req.body.continueUrl + s"?journeyId=$journeyId")))
                    }
                }
              }
            )
          } else Future.successful(BadRequest(Json.toJson("JourneyConfig contained non-relative urls")))
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def retrieveJourneyData(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
            journeyConfig =>  storageService.retrieveAllData(journeyId, journeyConfig).map(journeyDataJson => Ok(journeyDataJson))
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }
}

object JourneyController {
  val continueUrlKey = "continueUrl"
  val optServiceNameKey = "optServiceName"
  val deskProServiceIdKey = "deskProServiceId"
  val signOutUrlKey = "signOutUrl"
  val accessibilityUrlKey = "accessibilityUrl"
  val businessVerificationCheckKey = "businessVerificationCheck"
  val regimeKey = "regime"
  val journeyStartUrl = "journeyStartUrl"
}
