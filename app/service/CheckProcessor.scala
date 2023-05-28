package service

import java.nio.charset.CodingErrorAction
import scala.collection.mutable.ListBuffer
import scala.io.{Codec, Source}

object CheckProcessor {

  implicit val codec: Codec = Codec("iso-8859-1")
  codec.onMalformedInput(CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  def main(args: Array[String]): Unit = {
    // loading all data into memory
    val corpus_movie_conv = Source.fromFile("C:\\Users\\rcdo\\IdeaProjects\\ml-journey-notify-api\\app\\service\\movie_conversations.txt")
    val corpus_movie_lines = Source.fromFile("C:\\Users\\rcdo\\IdeaProjects\\ml-journey-notify-api\\app\\service\\movie_lines.txt")
    val conv = corpus_movie_conv.getLines().toList
    val lines = corpus_movie_lines.getLines().toList
    corpus_movie_conv.close()
    corpus_movie_lines.close()
    // splitting text using special lines
    val lines_dic: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map()
    for (line <- lines) {
      val objects = line.split("\\+\\+\\+\\$\\+\\+\\+")
      lines_dic.put(objects(0).strip(), objects.last.strip())
    }
    // generate question answer pairs
    val pair_list = ListBuffer[List[List[String]]]()
    conv.foreach(it => {
      val id_list = it.split("\\+\\+\\+\\$\\+\\+\\+").last.replace("[", "")
        .replace("]", "").split(",").map(it => it.strip())
      val qa_pairs = for (i <- id_list.indices;
          if i != id_list.length - 1;
          first = lines_dic(id_list(i).replaceAll("'", "").strip()).strip();
          second = lines_dic(id_list(i + 1).replaceAll("'", "").strip()).strip())
      yield List(first, second)
      pair_list += qa_pairs.toList

    })
  }
}
