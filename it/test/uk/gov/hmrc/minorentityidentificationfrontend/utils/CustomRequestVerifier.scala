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

package uk.gov.hmrc.minorentityidentificationfrontend.utils

import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.matching.{ContainsPattern, RequestPatternBuilder, UrlPathPattern}
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

object CustomRequestVerifier extends Eventually with IntegrationPatience {

  /**
   * Method checks for existence of an audit request of specific type for a given audit url
   */
  def verifyAuditRequests(method: RequestMethod, url: String, auditType: String): Unit = {

    val requestPatternBuilder: RequestPatternBuilder = new RequestPatternBuilder(method, new UrlPathPattern(new ContainsPattern(url), false))

    eventually {
      import scala.jdk.CollectionConverters._

      val allMatchedRequests: List[LoggedRequest] = WireMock.findAll(requestPatternBuilder).listIterator().asScala.toList

      // When debugging tests it is useful to print out the matched requests

      val auditTypes: List[String] = extractAuditTypes(allMatchedRequests)

      if(auditTypes.contains(auditType))()
      else
        throw new AssertionError(s"Error: Request with audit type $auditType not found")

    }
  }

  private def extractAuditTypes(allMatchedRequests: List[LoggedRequest]): List[String] = {
    allMatchedRequests.map(loggedRequest => (extractRequestBody(loggedRequest) \ "auditType").as[String])
  }

  private def extractRequestBody(request: LoggedRequest): JsValue = Try(Json.parse(request.getBodyAsString)) match {
    case Failure(_) => throw new IllegalStateException(s"Audit should receive json request but it did not. Request details:\n$request")
    case Success(value) => value
  }

}
