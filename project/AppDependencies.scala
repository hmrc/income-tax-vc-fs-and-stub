import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "10.6.0"
  private val hmrcMongoVersion = "2.12.0"
  private val circeVersion = "0.14.10"
  private val jsonSchemaValidatorVersion = "2.2.14"


  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"   % bootstrapVersion,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"          % hmrcMongoVersion,
    "com.github.java-json-tools" % "json-schema-validator" % jsonSchemaValidatorVersion,
    "io.circe"          %% "circe-core"                  % circeVersion,
    "io.circe"          %% "circe-parser"                % circeVersion,
    "io.circe"          %% "circe-generic"               % circeVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"      % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"     % hmrcMongoVersion % Test,
    "com.github.java-json-tools" % "json-schema-validator" % jsonSchemaValidatorVersion % Test

  )

  val it = Seq.empty
}