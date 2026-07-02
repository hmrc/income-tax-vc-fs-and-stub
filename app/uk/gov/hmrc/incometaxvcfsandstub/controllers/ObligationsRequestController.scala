/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest}
import play.api.{Configuration, Logging}
import uk.gov.hmrc.incometaxvcfsandstub.models.BusinessDetailsModel
import uk.gov.hmrc.incometaxvcfsandstub.repositories.DataRepository
import uk.gov.hmrc.incometaxvcfsandstub.utils.{AddDelays, BusinessDataUtils, ObligationsDataUtils}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ObligationsRequestController @Inject()(cc: MessagesControllerComponents,
                                             dataRepository: DataRepository)
                                            (implicit val ec: ExecutionContext,
                                                  val actorSystem: ActorSystem,
                                                  val configuration: Configuration)
    extends FrontendController(cc) with Logging with AddDelays {

  private def overrideObligationsUrl(nino: String): String = {
    s"/enterprise/obligation-data/nino/$nino/ITSA?status=F"
  }

  def overwriteObligationsData(nino: String): Action[AnyContent] =
    Action.async { implicit request =>
      val url = overrideObligationsUrl(nino)

      val obligationsData = ObligationsDataUtils.createObligationsData()

      for {
        obligationsUpdate <- dataRepository.clearAndReplace(url, ObligationsDataUtils.obligationsDataKey, obligationsData)
      } yield {
        if (obligationsUpdate.wasAcknowledged()) {
          logger.info("Successfully updated obligation details")
          Ok("Success")
        } else {
          logger.warn("Failed to update obligations")
          InternalServerError("Failed to update obligations")
        }
      }
    }
}
