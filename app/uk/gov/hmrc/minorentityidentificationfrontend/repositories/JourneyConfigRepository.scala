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

package uk.gov.hmrc.minorentityidentificationfrontend.repositories

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.json.{Format, JsObject, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.JourneyConfigRepository.{AuthInternalIdKey, CreationTimestampKey, JourneyIdKey, timeToLiveIndex}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConfigRepository @Inject()(mongoComponent: MongoComponent,
                                        appConfig: AppConfig)
                                       (implicit ec: ExecutionContext) extends PlayMongoRepository[JsObject](
  collectionName = "minor-entity-identification-frontend",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[JsObject]],
  indexes = Seq(timeToLiveIndex(appConfig.timeToLiveSeconds))
) {

  def insertJourneyConfig(journeyId: String, authInternalId: String, journeyConfig: JourneyConfig): Future[InsertOneResult] = {
    val document = Json.obj(
      JourneyIdKey -> journeyId,
      AuthInternalIdKey -> authInternalId,
      CreationTimestampKey -> Json.obj("$date" -> Instant.now.toEpochMilli)
    ) ++ Json.toJsObject(journeyConfig)

    collection.insertOne(document).toFuture()
  }

  def getJourneyConfig(journeyId: String): Future[Option[JsObject]] =
    collection.find(
      Filters.equal(JourneyIdKey, journeyId)
    ).headOption

  def drop: Future[Unit] = collection.drop().toFuture.map(_ => Unit)

}

object JourneyConfigRepository {
  val JourneyIdKey = "_id"
  val AuthInternalIdKey = "authInternalId"
  val CreationTimestampKey = "creationTimestamp"
  val BusinessEntityKey = "businessEntity"

  def timeToLiveIndex(timeToLiveDuration: Long): IndexModel = IndexModel(
    keys = ascending(CreationTimestampKey),
    indexOptions = IndexOptions()
      .name("MinorEntityIdentificationFrontendExpires")
      .expireAfter(timeToLiveDuration, TimeUnit.SECONDS)
  )
}
