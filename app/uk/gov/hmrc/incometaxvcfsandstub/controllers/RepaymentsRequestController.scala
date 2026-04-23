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
import org.mongodb.scala.model.Filters.*
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logging}
import uk.gov.hmrc.incometaxvcfsandstub.repositories.{DataRepository, DefaultValues}
import uk.gov.hmrc.incometaxvcfsandstub.utils.{AddDelays, RepaymentDataUtils}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RepaymentsRequestController @Inject()(cc: MessagesControllerComponents,
                                            dataRepository: DataRepository)
                                           (implicit val ec: ExecutionContext,
                                            val actorSystem: ActorSystem,
                                            val configuration: Configuration)
  extends FrontendController(cc) with Logging with AddDelays {

  private val overrideRepaymentsUrl: String = {
    s"/income-tax/self-assessment/repayments-viewer/AY888881A"

  }

  def overrideEstimatedRepaymentDate(): Action[AnyContent] =
    Action.async { implicit request =>
      for {
        oldRepaymentsData <- dataRepository.find(equal("_id", overrideRepaymentsUrl))
        repaymentsUpdate <- dataRepository.replaceOne(overrideRepaymentsUrl, RepaymentDataUtils.updateEstimatedRepaymentDate(oldRepaymentsData).get)
      } yield {
        if (repaymentsUpdate.wasAcknowledged()) {
          logger.info("Successfully updated repayments details")
          Ok("Success")
        } else {
          logger.warn("Failed to update repayments")
          InternalServerError("Failed to update repayments")
        }
      }
    }

}
