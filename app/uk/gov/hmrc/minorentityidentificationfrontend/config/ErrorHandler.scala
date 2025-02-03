/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.{Configuration, Environment, Logging, Mode}

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.Results.{NotFound, Redirect}
import play.api.mvc.{RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.minorentityidentificationfrontend.views.html.templates.error_template
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             view: error_template,
                             val config: Configuration,
                             val env: Environment
                            )(implicit val appConfig: AppConfig, implicit val ec: ExecutionContext) extends FrontendErrorHandler with Logging  {

  override def standardErrorTemplate(pageTitle: String,
                                     heading: String,
                                     message: String
                                    )(implicit request: RequestHeader): Future[Html] =
    Future.successful(view(pageTitle, heading, message))


  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case _: AuthorisationException => resolveError(request, exception)
      case _ => super.onServerError(request, exception)
    }
  }

  val hostDefaults: Map[String, String] = Map(
    "Dev.external-url.bas-gateway-frontend.host" -> appConfig.basGatewayUrl
  )

  private lazy val envPrefix =
    if (env.mode.equals(Mode.Test)) "Test"
    else config.getOptional[String]("run.mode")
      .getOrElse("Dev")

  private def basGatewayUrl(): String = {
    val key = s"$envPrefix.external-url.bas-gateway-frontend.host"
    config.getOptional[String](key).orElse(hostDefaults.get(key)).getOrElse("")
  }

  private def ggLoginUrl: String = basGatewayUrl() + "/bas-gateway/sign-in"

  override def resolveError(rh: RequestHeader, ex: Throwable): Future[Result] = {
    ex match {
      case _: AuthorisationException =>
        logger.debug("[AuthenticationPredicate][async] Unauthorised request. Redirect to Sign In.")
        Future.successful(Redirect(
          ggLoginUrl,
          Map(
          "continue_url" -> Seq(rh.path),
          "origin" -> Seq(appConfig.appName)
        )))
      case _: NotFoundException =>
        notFoundTemplate(rh).map(html => NotFound(html))
      case _ =>
        super.resolveError(rh, ex)
    }
  }
}
