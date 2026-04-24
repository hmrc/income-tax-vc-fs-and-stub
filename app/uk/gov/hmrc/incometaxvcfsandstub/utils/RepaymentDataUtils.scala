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

package uk.gov.hmrc.incometaxvcfsandstub.utils

import play.api.libs.json.*
import uk.gov.hmrc.incometaxvcfsandstub.models.DataModel

import java.time.LocalDate

object RepaymentDataUtils {
  private val todayValue = JsString(LocalDate.now().toString)

  private def updateByIndex(index: Int): Reads[JsObject] = {
    (__ \ "repaymentsViewerDetails").json.update(
      Reads.list(__.json.pick).map { items =>
        val updated = items.zipWithIndex.map {
          case (item, i) if i == index =>
            item.as[JsObject] + ("estimatedRepaymentDate" -> todayValue)
          case (item, _) => item
        }
        JsArray(updated)
      }
    )
  }

  private def updateByIndexHip(index: Int): Reads[JsObject] = {
    (__ \ "etmp_Response_Details" \ "repaymentsViewerDetails").json.update(
      Reads.list(__.json.pick).map { items =>
        val updated = items.zipWithIndex.map {
          case (item, i) if i == index =>
            item.as[JsObject] + ("estimatedRepaymentDate" -> todayValue)
          case (item, _) => item
        }
        JsArray(updated)
      }
    )
  }

  def updateEstimatedRepaymentDate(oldRecord: Option[DataModel], index: Int = 0): Option[DataModel] = {
    for {
      record <- oldRecord
      response <- record.response
      updatedResponse <- response.transform(updateByIndex(index)).asOpt
    } yield record.copy(response = Some(updatedResponse))
  }

  def updateEstimatedRepaymentDateHip(oldRecord: Option[DataModel], index: Int = 0): Option[DataModel] = {
    for {
      record <- oldRecord
      response <- record.response
      updatedResponse <- response.transform(updateByIndexHip(index)).asOpt
    } yield record.copy(response = Some(updatedResponse))
  }
}
