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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors

import javax.inject.Inject
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.ValidateUnincorporatedAssociationDetailsHttpParser._
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatchingResult

import scala.concurrent.{ExecutionContext, Future}

class ValidateUnincorporatedAssociationDetailsConnector @Inject()(httpClient: HttpClientV2,
                                                                  appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def validateUnincorporatedAssociationDetails(ctUtr: String, postcode: String)
                                              (implicit hc: HeaderCarrier): Future[KnownFactsMatchingResult] = {

    val requestBody: JsObject = Json.obj(
      "ctutr" -> ctUtr,
      "postcode" -> postcode
    )

    httpClient.post(url"${appConfig.validateUnincorporatedAssociationDetailsUrl}").withBody(Json.toJson(requestBody)).execute
  }

}
