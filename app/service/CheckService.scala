package service

import com.johnsnowlabs.nlp.DocumentAssembler
import com.johnsnowlabs.nlp.annotator.SentenceEmbeddings
import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.base.EmbeddingsFinisher
import com.johnsnowlabs.nlp.embeddings.WordEmbeddingsModel
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.SparkSession

import java.nio.charset.CodingErrorAction
import java.util.UUID
import scala.io.Codec
import scala.math._

object CheckService {

  implicit val codec: Codec = Codec("iso-8859-1")
  codec.onMalformedInput(CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  def checkGoal(goal: String, action: String): Double = {
    if (goal == null || action == null)
      0
    val yourGoal: List[Float] = toListFloat(createWordEmbedding(goal).toString)
    val yourAction: List[Float] = toListFloat(createWordEmbedding(action).toString)
    cosineSimilarity(yourGoal, yourAction)
  }

  def cosineSimilarity(vec1: List[Float], vec2: List[Float]): Double = {
    val sumVec1Vec2 = (for (i <- vec1.indices)
      yield vec1(i) * vec2(i)).sum
    val sumVec1 = sqrt((for (
      i <- vec1.indices
    ) yield pow(vec1(i), 2)).sum)
    val sumVec2 = sqrt((for (
      i <- vec2.indices
    ) yield pow(vec2(i), 2)).sum)
    sumVec1Vec2 / (sumVec1 * sumVec2)
  }

  def toListFloat(text: String): List[Float] = {
    val textStr = text.replace("WrappedArray([", "").replace("])", "")
    val textArr = textStr.split(",")
    (for (i <- 0 until textArr.length) yield textArr(i).toFloat).toArray[Float].toList
  }

  def createWordEmbedding(text: String): Any = {
    val spark: SparkSession = SparkSession.builder
      .appName("test")
      .config("spark.master", "local[*]")
      .getOrCreate
    import spark.implicits._
    val documentAssembler = new DocumentAssembler()
      .setInputCol("text")
      .setOutputCol("document")
    val tokeniser = new Tokenizer()
      .setInputCols(Array("document"))
      .setOutputCol("token")
    val embeddings = WordEmbeddingsModel.pretrained()
      .setInputCols("document", "token")
      .setOutputCol("embeddings")
    val embeddingsSentence = new SentenceEmbeddings()
      .setInputCols(Array("document", "embeddings"))
      .setOutputCol("sentence_embeddings")
      .setPoolingStrategy("AVERAGE")
    val embeddingsEnd = new EmbeddingsFinisher()
      .setInputCols("sentence_embeddings")
      .setOutputCols("last_embeddings")
      .setOutputAsVector(true)
      .setCleanAnnotations(false)
    val pipeLine = new Pipeline()
      .setStages(Array(
        documentAssembler,
        tokeniser,
        embeddings,
        embeddingsSentence,
        embeddingsEnd
      ))
    val data = Seq(text).toDF("text")
    pipeLine
      .fit(data).transform(data)
      .select("last_embeddings").collectAsList()
      .get(0).get(0)
  }

  def generateReqNo(): String = {
    val randNum = UUID.randomUUID().toString.substring(0, 7)
    "REQ" + randNum
  }
}
