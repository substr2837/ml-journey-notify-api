package entity
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

import java.sql.Date
import java.time.LocalDate

class CheckHistory(tag: Tag)extends Table[(Int, String, String, LocalDate)](tag, "check_history"){
  implicit val localDateColumnType: JdbcType[LocalDate]
    with BaseTypedType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    it => Date.valueOf(it),
    it => it.toLocalDate
  )
  def id = column[Int]("id", O.PrimaryKey)
  def account_id = column[Int]("account_id")
  def user_goal = column[String]("user_goal")
  def user_action = column[String]("user_action")
  def created_at =  column[LocalDate]("created_at")(localDateColumnType)
  def modified_at = column[LocalDate]("modified_at")(localDateColumnType)
  override def * = (account_id, user_goal, user_action, created_at)
}
