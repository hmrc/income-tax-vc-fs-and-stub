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

package uk.gov.hmrc.incometaxvcfsandstub.repositories

import controllers.Execution.trampoline
import uk.gov.hmrc.incometaxvcfsandstub.models.DataModel
import uk.gov.hmrc.mongo.logging.ObservableFutureImplicits.*
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonDocument, BsonValue}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.{Filters, ReplaceOptions, UpdateOptions, Updates}
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import scala.jdk.CollectionConverters.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class DataRepository @Inject() (repository: DataRepositoryBase) {

  def removeAll(): Future[DeleteResult] = repository.collection.deleteMany(empty()).toFuture()

  def removeById(url: String): Future[DeleteResult] = repository.collection.deleteOne(equal("_id", url)).toFuture()

  def removeByIdPrefix(prefix: String): Future[DeleteResult] = repository.collection.deleteMany(Filters.regex("_id", s"^${java.util.regex.Pattern.quote(prefix)}")).toFuture()

  def addEntry(document: DataModel): Future[UpdateResult] =
    repository.collection
      .replaceOne(
        equal("_id", document._id),
        document,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()

  def find(query: Bson*): Future[Option[DataModel]] = {
    val finalQuery = if (query.isEmpty) empty() else and(query: _*)
    repository.collection.find(finalQuery).headOption()
  }

  def replaceOne(url: String, updatedFile: DataModel): Future[UpdateResult] = {
    repository.collection
      .replaceOne(
        filter = Filters.equal("_id", url),
        replacement = updatedFile,
        options = new ReplaceOptions().upsert(true)
      )
      .toFuture()
  }

  def clearAndReplace(url: String, arrayField: String, newArray: Seq[Document]): Future[UpdateResult] = {
    val filter = Filters.equal("_id", url)
    val bsonDocs: Seq[BsonDocument] = newArray.map(_.toBsonDocument)
    val updates = Updates.set(arrayField, bsonDocs)


    repository.collection
      .updateOne(filter, updates, UpdateOptions().upsert(true))
      .toFuture()
  }

  def replaceObjectField(url: String, objectFieldPath: String, newObject: Document): Future[UpdateResult] = {
    val filter = Filters.equal("_id", url)
    val bsonObject: BsonDocument = newObject.toBsonDocument
    val update = Updates.set(objectFieldPath, bsonObject)

    repository.collection
      .updateOne(filter, update, UpdateOptions().upsert(true))
      .toFuture()
  }

  def updateArrayItemFieldByIdentifier(
                                        url: String,
                                        arrayPath: String, // e.g. "response.success.documentDetails"
                                        identifierField: String, // e.g. "documentId"
                                        identifierValue: BsonValue, // e.g. BsonString("888881202203")
                                        targetField: String, // e.g. "chargeClassification"
                                        newValue: BsonValue
                                      ): Future[UpdateResult] = {
    val filter = Filters.equal("_id", url)
    val alias = "item"

    val updatePath = s"$arrayPath.$$[$alias].$targetField"
    val update = Updates.set(updatePath, newValue)

    val arrayFilter = BsonDocument(s"$alias.$identifierField" -> identifierValue)
    val options = new UpdateOptions().arrayFilters(List(arrayFilter).asJava)

    repository.collection.updateOne(filter, update, options).toFuture()
  }
  
}
