package entity

import org.joda.time.LocalDate
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.sql.Date

class Account(tag: Tag) extends Table[(String)](tag, "account"){
  protected implicit val localDateColumnType: JdbcType[LocalDate]
    with BaseTypedType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    it => Date.valueOf(it.toString),
    it => LocalDate.parse(it.toString)
  )
  def id = column[Int]("id",O.PrimaryKey)
  def address = column[String]("address")
  def latest_access = column[LocalDate]("latest_access")(localDateColumnType)
  def joined_at = column[LocalDate]("joined_at", SqlType("timestamp not null default now()"))
  override def * = (address)
}
