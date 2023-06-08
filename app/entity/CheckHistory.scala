package entity
import org.joda.time.LocalDate
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.sql.Date

class CheckHistory(tag: Tag)extends Table[(Int, String, String, String, LocalDate, String)](tag, "check_history"){
  protected implicit val localDateColumnType: JdbcType[LocalDate]
    with BaseTypedType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    it => Date.valueOf(it.toString),
    it => LocalDate.parse(it.toString)
  )
  def id = column[Int]("id", O.PrimaryKey)
  def account_id = column[Int]("account_id")
  def user_goal = column[String]("user_goal")
  def user_action = column[String]("user_action")
  def check_result = column[String]("check_result")
  def created_at =  column[LocalDate]("created_at", SqlType("timestamp not null default now()"))(localDateColumnType)
  def modified_at = column[LocalDate]("modified_at", SqlType("timestamp not null default now()"))(localDateColumnType)
  def request_no = column[String]("request_no")
  override def * = (account_id, user_goal, user_action, check_result, created_at, request_no)
}
