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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsArray, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import uk.gov.hmrc.incometaxvcfsandstub.controllers.helpers.DataHelper
import uk.gov.hmrc.incometaxvcfsandstub.mocks.MockDataRepository
import uk.gov.hmrc.incometaxvcfsandstub.models.DataModel
import uk.gov.hmrc.incometaxvcfsandstub.testUtils.TestSupport

import scala.concurrent.Future

class RepaymentsRequestControllerSpec extends TestSupport with MockDataRepository with DataHelper {

  val TestRepaymentsRequestController = new RepaymentsRequestController(mockCC, mockDataRepository)

  val record = DataModel(
    _id = "/income-tax/self-assessment/repayments-viewer/AY888881A",
    schemaId = "testID1",
    method = "GET",
    status = OK,
    response = Some(Json.obj(
      "repaymentsViewerDetails" -> JsArray(Seq(
        Json.obj(
          "repaymentId"            -> "REPAY001",
          "estimatedRepaymentDate" -> "2025-01-01",
          "amount"                 -> 100
        )
      ))
    ))
  )

  "overrideEstimatedRepaymentDate" should {
    "return status OK" when {
      "the repayments data is successfully updated" in {
        lazy val request = FakeRequest()
        when(mockDataRepository.find(any())).thenReturn(Future.successful(Some(record)))
        when(mockDataRepository.replaceOne(any(), any())).thenReturn(Future.successful(successUpdateResult))

        val result = TestRepaymentsRequestController.overrideEstimatedRepaymentDate()(request)

        status(result) shouldBe OK
        contentAsString(result) shouldBe "Success"
      }
    }

    "return status InternalServerError" when {
      "the repository update is not acknowledged" in {
        lazy val request = FakeRequest()
        when(mockDataRepository.find(any())).thenReturn(Future.successful(Some(record)))
        when(mockDataRepository.replaceOne(any(), any())).thenReturn(Future.successful(failedUpdateResult))

        val result = TestRepaymentsRequestController.overrideEstimatedRepaymentDate()(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to update repayments"
      }
    }
  }
}