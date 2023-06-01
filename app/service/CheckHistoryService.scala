package service

import entity.{Account, CheckHistory}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CheckHistoryService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def createAccount(address: String):Future[Unit]= {
    val account = TableQuery[Account]
    val accountAction = DBIO.seq(
      account += (address, new LocalDate(), new LocalDate())
    )
    db.run(accountAction)
  }

  def createHistory(account_id: Int, usergoal: String, useraction: String): Future[Unit] = {
    val checkHistory = TableQuery[CheckHistory]
    val checkHistoryAction = DBIO.seq(
      checkHistory += (account_id, usergoal, useraction, new LocalDate())
    )
    db.run(checkHistoryAction)
  }

  def checkAccountIsExist(address: String): Int = {
    val account = TableQuery[Account]
    val query: Future[List[Int]] = db.run{
      account.filter(_.address === address).map(_.id).to[List].result
    }
    Option(Await.result(query, 1.seconds).head).getOrElse(0)
  }

}