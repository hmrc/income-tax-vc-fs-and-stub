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

import uk.gov.hmrc.incometaxvcfsandstub.models.DataModel
import java.time.LocalDate
import play.api.libs.json._

object FinancialDetailsUtils {

  final val financialDetailsDataKey = "response.success"

  private val todayValue = JsString(LocalDate.now().toString)

  private val updateSingleDoc: Reads[JsObject] = __.json.update(
    (__ \ "effectiveDateOfPayment").json.put(todayValue)
  )
  private val documentDetailsTransformer: Reads[JsObject] =
    (__ \ "success" \ "documentDetails").json.update(
      Reads.list(updateSingleDoc).map(JsArray(_))
    )

  def updateEffectiveDateOfPayment(oldRecord: Option[DataModel]) = {
    for {
      record          <- oldRecord
      response        <- record.response
      updatedResponse <- response.transform(documentDetailsTransformer).asOpt
    } yield record.copy(response = Some(updatedResponse))
  }
}
