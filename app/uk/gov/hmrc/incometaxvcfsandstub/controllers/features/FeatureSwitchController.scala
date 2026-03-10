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

package uk.gov.hmrc.incometaxvcfsandstub.controllers.features

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.incometaxvcfsandstub.models.{FeatureSwitch, FeatureSwitchName}
import uk.gov.hmrc.incometaxvcfsandstub.repositories.FeatureSwitchRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FeatureSwitchController @Inject()(
                                         cc: ControllerComponents,
                                         featureSwitchRepository: FeatureSwitchRepository
                                       )(implicit ec: ExecutionContext) extends BackendController(cc) {
  
  def disableAll: Action[AnyContent] = Action.async {
    featureSwitchRepository.setFeatureSwitches(FeatureSwitchName.allFeatureSwitches.map(_ -> false).toMap).map{ _ =>
      Status(NO_CONTENT)
    }
  }

  def enableAll: Action[AnyContent] = Action.async {
    featureSwitchRepository.setFeatureSwitches(FeatureSwitchName.allFeatureSwitches.map(_ -> true).toMap).map{ _ =>
      Status(NO_CONTENT)
    }
  }

  def getAll: Action[AnyContent] = Action.async {
    featureSwitchRepository.getFeatureSwitches.map { featureSwitches =>
      Status(OK)(Json.toJson(featureSwitches))
    }
  }

  def resetToProd: Action[AnyContent] = Action.async {
    featureSwitchRepository.deleteAllFeatureSwitches().map { _ =>
      Status(NO_CONTENT)
    }
  }
}