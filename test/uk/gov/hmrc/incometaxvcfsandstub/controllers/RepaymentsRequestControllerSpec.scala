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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
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

  private val desUrl = "/income-tax/self-assessment/repayments-viewer/AY888881A"
  private val hipUrl = "/etmp/RESTAdapter/ITSA/RepaymentsViewer/AY888881A"

  val desRecord = DataModel(
    _id = desUrl,
    schemaId = "testID1",
    method = "GET",
    status = OK,
    response = Some(Json.obj(
      "repaymentsViewerDetails" -> JsArray(Seq(
        Json.obj(
          "repaymentId" -> "REPAY001",
          "estimatedRepaymentDate" -> "2025-01-01",
          "amount" -> 100
        )
      ))
    ))
  )

  val hipRecord = DataModel(
    _id = hipUrl,
    schemaId = "testID2",
    method = "GET",
    status = OK,
    response = Some(Json.obj(
      "etmp_Response_Details" -> Json.obj(
        "repaymentsViewerDetails" -> JsArray(Seq(
          Json.obj(
            "repaymentId" -> "REPAY002",
            "estimatedRepaymentDate" -> "2025-01-01",
            "amount" -> 200
          )
        ))
      )
    ))
  )

  "overrideEstimatedRepaymentDate" should {
    "return status OK" when {
      "both DES and HIP repayments data are successfully updated" in {
        lazy val request = FakeRequest()
        mockFindSequential(Some(desRecord), Some(hipRecord))
        when(mockDataRepository.replaceOne(eqTo(desUrl), any())).thenReturn(Future.successful(successUpdateResult))
        when(mockDataRepository.replaceOne(eqTo(hipUrl), any())).thenReturn(Future.successful(successUpdateResult))

        val result = TestRepaymentsRequestController.overrideEstimatedRepaymentDate()(request)

        status(result) shouldBe OK
        contentAsString(result) shouldBe "Success"
      }
    }

    "return status InternalServerError" when {
      "the DES repository update is not acknowledged" in {
        lazy val request = FakeRequest()
        mockFindSequential(Some(desRecord), Some(hipRecord))
        when(mockDataRepository.replaceOne(eqTo(desUrl), any())).thenReturn(Future.successful(failedUpdateResult))
        when(mockDataRepository.replaceOne(eqTo(hipUrl), any())).thenReturn(Future.successful(successUpdateResult))

        val result = TestRepaymentsRequestController.overrideEstimatedRepaymentDate()(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to update repayments"
      }

      "the HIP repository update is not acknowledged" in {
        lazy val request = FakeRequest()
        mockFindSequential(Some(desRecord), Some(hipRecord))
        when(mockDataRepository.replaceOne(eqTo(desUrl), any())).thenReturn(Future.successful(successUpdateResult))
        when(mockDataRepository.replaceOne(eqTo(hipUrl), any())).thenReturn(Future.successful(failedUpdateResult))

        val result = TestRepaymentsRequestController.overrideEstimatedRepaymentDate()(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to update repayments"
      }

      "both DES and HIP repository updates are not acknowledged" in {
        lazy val request = FakeRequest()
        mockFindSequential(Some(desRecord), Some(hipRecord))
        when(mockDataRepository.replaceOne(eqTo(desUrl), any())).thenReturn(Future.successful(failedUpdateResult))
        when(mockDataRepository.replaceOne(eqTo(hipUrl), any())).thenReturn(Future.successful(failedUpdateResult))

        val result = TestRepaymentsRequestController.overrideEstimatedRepaymentDate()(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to update repayments"
      }
    }
  }
}