package entity

import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

import java.sql.Date
import java.time.LocalDate

class Account(tag: Tag) extends Table[(String, LocalDate, LocalDate)](tag, "account"){
  protected implicit val localDateColumnType: JdbcType[LocalDate] with BaseTypedType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    it => Date.valueOf(it),
    it => it.toLocalDate
  )
  def id = column[Int]("id",O.PrimaryKey)
  def address = column[String]("address")
  def latest_access = column[LocalDate]("latest_access")(localDateColumnType)
  def joined_at = column[LocalDate]("joined_at")
  override def * = (address, joined_at, latest_access)
}
