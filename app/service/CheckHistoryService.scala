package service

import entity.{Account, CheckHistory}
import org.joda.time.LocalDate
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.ast.BaseTypedType
import slick.jdbc.{JdbcProfile, JdbcType}

import java.sql.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class CheckHistoryService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                   (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  protected implicit val localDateColumnType: JdbcType[LocalDate]
    with BaseTypedType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    it => Date.valueOf(it.toString),
    it => LocalDate.parse(it.toString)
  )

  def createAccount(address: String): Int = {
    val account = TableQuery[Account]
    val accountAction = DBIO.seq(
      account += (address)
    )
    Await.result(db.run(accountAction), 1.seconds)
    val accountQuery = db.run {
      account.sortBy(_.joined_at).map(_.id).to[List].result
    }
    Option(Await.result(accountQuery, 4.seconds).head).getOrElse(0)
  }

  def modifyLatestAccess(id: Int): Unit = {
    val account = TableQuery[Account]
    val q = for {a <- account if a.id === id} yield a.latest_access
    val updateAction = q.update(LocalDate.now())
    Await.result(db.run(updateAction), 4.seconds)
  }

  def createHistory(account_id: Int, usergoal: String, useraction: String, check_result: String, request_no: String): Unit = {
    val checkHistory = TableQuery[CheckHistory]
    val checkHistoryAction = DBIO.seq(
      checkHistory += (account_id, usergoal, useraction, check_result, LocalDate.now(), request_no)
    )
    Await.result(db.run(checkHistoryAction), 4.seconds)
  }

  def checkAccountIsExist(address: String): Int = {
    val account = TableQuery[Account]
    val query: Future[List[Int]] = db.run {
      account.filter(_.address === address).map(_.id).to[List].result
    }
    val result = Option(Await.result(query, 4.seconds)).get
    if (result.isEmpty) 0 else result.head
  }

  def getCheckResult(request_no: String): String = {
    val checkHistory = TableQuery[CheckHistory]
    val query: Future[List[String]] = db.run {
      checkHistory.filter(_.request_no === request_no).map(_.check_result).to[List].result
    }
    val result = Option(Await.result(query, 4.seconds)).get
    if (result.isEmpty) "not ready" else result.head
  }
}