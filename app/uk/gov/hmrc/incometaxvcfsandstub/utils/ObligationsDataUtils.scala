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

import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

object ObligationsDataUtils {
  private val today = LocalDate.now()

  def createFulfilledObligationsData(): JsValue = {
    val currentTaxYearStart =
      if (today.isBefore(LocalDate.of(today.getYear, 4, 6)))
        LocalDate.of(today.minusYears(1).getYear, 4, 6)
      else
        LocalDate.of(today.getYear, 4, 6)

    val taxYearTaxReturnDueDate =
      LocalDate.of(currentTaxYearStart.getYear + 1, 1, 31)

    Json.obj(
      "obligations" -> Json.arr(
        Json.obj(
          "identification" -> Json.obj(
            "incomeSourceType" -> "ITSB",
            "referenceNumber" -> "XAIS00000000001",
            "referenceType" -> "MTDBIS"
          ),
          "obligationDetails" -> Json.arr(
            Json.obj(
              "status" -> "F",
              "inboundCorrespondenceFromDate" -> currentTaxYearStart.minusYears(1).toString,
              "inboundCorrespondenceToDate" -> currentTaxYearStart.minusDays(1).toString,
              "inboundCorrespondenceDueDate" -> taxYearTaxReturnDueDate.toString,
              "inboundCorrespondenceDateReceived" -> today.minusMonths(1).toString,
              "periodKey" -> "#001"
            )
          )
        ),
        Json.obj(
          "identification" -> Json.obj(
            "incomeSourceType" -> "ITSA",
            "referenceNumber" -> "XAIT00000000002",
            "referenceType" -> "MTDBIS"
          ),
          "obligationDetails" -> Json.arr(
            Json.obj(
              "status" -> "F",
              "inboundCorrespondenceFromDate" -> currentTaxYearStart.minusYears(1).toString,
              "inboundCorrespondenceToDate" -> currentTaxYearStart.minusDays(1).toString,
              "inboundCorrespondenceDueDate" -> taxYearTaxReturnDueDate.toString,
              "inboundCorrespondenceDateReceived" -> today.toString,
              "periodKey" -> "C"
            )
          )
        )
      )
    )
  }
}
