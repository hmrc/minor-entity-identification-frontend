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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers

import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.BaseBusinessVerificationController
import uk.gov.hmrc.minorentityidentificationfrontend.services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessVerificationController @Inject()(mcc: MessagesControllerComponents,
                                               val authConnector: AuthConnector,
                                               businessVerificationService: BusinessVerificationService,
                                               journeyService: JourneyService,
                                               storageService: StorageService,
                                               registrationOrchestrationService: RegistrationOrchestrationService
                                              )(implicit override val executionContext: ExecutionContext)
  extends BaseBusinessVerificationController(mcc, businessVerificationService, journeyService, storageService, registrationOrchestrationService)