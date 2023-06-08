package entity

import slick.lifted._
import slick.jdbc.PostgresProfile.api._

class ServerParameter(tag: Tag) extends Table[(String, String)](tag, "server_parameter"){
  def id = column[Int]("id", O.PrimaryKey)
  def key = column[String]("key")
  def value = column[String]("value")
  override def * = (key, value)
}
