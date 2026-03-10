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

package uk.gov.hmrc.incometaxvcfsandstub.controllers

import org.apache.pekko.actor.ActorSystem
import play.api.Configuration
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxvcfsandstub.models.FeatureSwitchName
import uk.gov.hmrc.incometaxvcfsandstub.repositories.FeatureSwitchRepository
import uk.gov.hmrc.incometaxvcfsandstub.utils.AddDelays
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeatureSwitchController @Inject()(cc: MessagesControllerComponents,
                                        featureSwitchRepo: FeatureSwitchRepository)
                                       (implicit val ec: ExecutionContext,
                                        val actorSystem: ActorSystem,
                                        val configuration: Configuration
                                       )
  extends FrontendController(cc) with AddDelays {

  def setFeatureSwitch(featureName: String, isEnabled: Boolean): Action[AnyContent] =
    Action.async { implicit request =>
      FeatureSwitchName.get(featureName) match {
        case Some(feature) =>
          withDelay(700.milliseconds) {
            featureSwitchRepo.setFeatureSwitch(feature, isEnabled)
            Future.successful(NoContent)
          }
        case None =>
          Future.successful(BadRequest(s"Unknown feature switch: $featureName"))
      }
    }
}