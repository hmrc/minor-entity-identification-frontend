/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.mvc.ControllerComponents
import play.filters.csp._
import play.api.mvc._
import javax.inject.Inject



class CSPReportController @Inject() (cc: ControllerComponents, cspReportAction: CSPReportActionBuilder)
  extends AbstractController(cc) {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  val report: Action[ScalaCSPReport] = cspReportAction { request =>
    val report = request.body
    logger.warn(
      s"CSP violation: violated-directive = ${report.violatedDirective}, " +
        s"blocked = ${report.blockedUri}, " +
        s"policy = ${report.originalPolicy}"
    )
    Ok("{}").as(JSON)
  }
}