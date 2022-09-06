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

package uk.gov.hmrc.minorentityidentificationfrontend.config

import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.{Configuration, Environment, Logging}

import javax.inject.{Inject, Singleton}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results.{InternalServerError, NotFound}
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.minorentityidentificationfrontend.errors.JourneyException
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.minorentityidentificationfrontend.services.JourneyService
import uk.gov.hmrc.minorentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.templates.{error_template, journey_error_template}
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.Future

import scala.concurrent.ExecutionContext

@Singleton
class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             val messagesHelper: MessagesHelper,
                             val journeyService: JourneyService,
                             view: error_template,
                             journeyView: journey_error_template,
                             val config: Configuration,
                             val env: Environment
                            )(implicit val appConfig: AppConfig,
                              executionContext: ExecutionContext) extends FrontendErrorHandler with AuthRedirects with Logging  {

  override def standardErrorTemplate(pageTitle: String,
                                     heading: String,
                                     message: String
                                    )(implicit request: Request[_]): Html =
    view(pageTitle, heading, message)


  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case _: AuthorisationException => Future.successful(resolveError(request, exception))
      case jEx : JourneyException => resolveJourneyException(request, jEx)
      case _ => super.onServerError(request, exception)
    }
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = {
    ex match {
      case _: AuthorisationException =>
        logger.debug("[AuthenticationPredicate][async] Unauthorised request. Redirect to Sign In.")
        toGGLogin(rh.path)
      case _: NotFoundException =>
        NotFound(notFoundTemplate(Request(rh, "")))
      case _ =>
        super.resolveError(rh, ex)
    }
  }

  private def resolveJourneyException(rh: RequestHeader, journeyException: JourneyException): Future[Result] = {

    logException(rh, journeyException)

    journeyService.getJourneyConfig(journeyException.journeyId, journeyException.authInternalId).map {
      journeyConfig =>
      InternalServerError(journeyErrorTemplate(journeyConfig)(Request(rh, ""))).withHeaders(CACHE_CONTROL -> "no-cache")
    } recover {
      case ex: Exception => super.resolveError(rh, ex)
    }

  }

  private def journeyErrorTemplate(journeyConfig: JourneyConfig)(implicit request: Request[_]): Html = {

    val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)

    journeyView(
      "global.error.InternalServerError500.title",
      "global.error.InternalServerError500.heading",
      "global.error.InternalServerError500.message",
      journeyConfig.pageConfig
    )(request, messages, appConfig)

  }

  private def logException(request: RequestHeader, ex: Throwable): Unit =
    logger.error(
      """
        |
        |! Journey exception, for (%s) [%s] ->
        | """.stripMargin.format(request.method, request.uri),
      ex
    )

}
