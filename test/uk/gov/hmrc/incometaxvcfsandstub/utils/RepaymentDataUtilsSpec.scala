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

import play.api.http.Status
import play.api.libs.json.{JsArray, JsObject, Json}
import uk.gov.hmrc.incometaxvcfsandstub.models.DataModel
import uk.gov.hmrc.incometaxvcfsandstub.testUtils.TestSupport

import java.time.LocalDate

class RepaymentDataUtilsSpec extends TestSupport {

  private val today = LocalDate.now().toString

  private def makeDataModel(repaymentsJson: JsObject): DataModel =
    DataModel(
      _id = "/income-tax/self-assessment/repayments-viewer/AY888881A",
      schemaId = "testID1",
      method = "GET",
      status = Status.OK,
      response = Some(repaymentsJson)
    )

  private def repaymentEntry(estimatedDate: String): JsObject = Json.obj(
    "repaymentId"             -> "REPAY001",
    "estimatedRepaymentDate"  -> estimatedDate,
    "amount"                  -> 100
  )

  private def responseWithEntries(entries: Seq[JsObject]): JsObject = Json.obj(
    "repaymentsViewerDetails" -> JsArray(entries)
  )

  "updateEstimatedRepaymentDate" should {

    "update the estimatedRepaymentDate of the first entry by default (index 0)" in {
      val original = makeDataModel(responseWithEntries(Seq(repaymentEntry("2025-01-01"))))

      val result = RepaymentDataUtils.updateEstimatedRepaymentDate(Some(original))

      result shouldBe defined
      val entries = (result.get.response.get \ "repaymentsViewerDetails").as[JsArray].value
      (entries(0) \ "estimatedRepaymentDate").as[String] shouldBe today
    }

    "update only the entry at the specified index, leaving others unchanged" in {
      val original = makeDataModel(responseWithEntries(Seq(
        repaymentEntry("2025-01-01"),
        repaymentEntry("2025-02-01"),
        repaymentEntry("2025-03-01")
      )))

      val result = RepaymentDataUtils.updateEstimatedRepaymentDate(Some(original), index = 1)

      result shouldBe defined
      val entries = (result.get.response.get \ "repaymentsViewerDetails").as[JsArray].value
      (entries(0) \ "estimatedRepaymentDate").as[String] shouldBe "2025-01-01"
      (entries(1) \ "estimatedRepaymentDate").as[String] shouldBe today
      (entries(2) \ "estimatedRepaymentDate").as[String] shouldBe "2025-03-01"
    }

    "preserve all other fields on the updated entry" in {
      val original = makeDataModel(responseWithEntries(Seq(repaymentEntry("2025-01-01"))))

      val result = RepaymentDataUtils.updateEstimatedRepaymentDate(Some(original))

      result shouldBe defined
      val entry = (result.get.response.get \ "repaymentsViewerDetails").as[JsArray].value.head.as[JsObject]
      (entry \ "repaymentId").as[String] shouldBe "REPAY001"
      (entry \ "amount").as[Int] shouldBe 100
    }

    "return None when the input record is None" in {
      val result = RepaymentDataUtils.updateEstimatedRepaymentDate(None)

      result shouldBe None
    }

    "return None when the record has no response" in {
      val record = DataModel(
        _id = "/income-tax/self-assessment/repayments-viewer/AY888881A",
        schemaId = "testID1",
        method = "GET",
        status = Status.OK,
        response = None
      )

      val result = RepaymentDataUtils.updateEstimatedRepaymentDate(Some(record))

      result shouldBe None
    }

    "return None when the response does not contain repaymentsViewerDetails" in {
      val record = makeDataModel(Json.obj("someOtherField" -> "value"))

      val result = RepaymentDataUtils.updateEstimatedRepaymentDate(Some(record))

      result shouldBe None
    }

    "handle a single-entry list correctly" in {
      val original = makeDataModel(responseWithEntries(Seq(repaymentEntry("2024-06-15"))))

      val result = RepaymentDataUtils.updateEstimatedRepaymentDate(Some(original), index = 0)

      result shouldBe defined
      val entries = (result.get.response.get \ "repaymentsViewerDetails").as[JsArray].value
      entries.size shouldBe 1
      (entries(0) \ "estimatedRepaymentDate").as[String] shouldBe today
    }
  }
}
