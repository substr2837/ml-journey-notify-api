package service

import entity.{Account, CalculateResponse, CheckHistory}
import org.joda.time.LocalDate
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.ast.BaseTypedType
import slick.jdbc.{JdbcProfile, JdbcType}

import java.sql.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class HomePageService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, val serverParameterService: ServerParameterService)
                                   (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  protected implicit val localDateColumnType: JdbcType[LocalDate]
    with BaseTypedType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    it => Date.valueOf(it.toString),
    it => LocalDate.parse(it.toString)
  )

  def calculate(address:String):CalculateResponse = {
    val account = TableQuery[Account]
    val checkHistory = TableQuery[CheckHistory]
    val query: Future[List[Int]] = db.run {
      account.filter(_.address === address).map(_.id).to[List].result
    }
    val result = Await.result(query, 4.seconds)
    if(result.isEmpty)
      throw new RuntimeException("Account not found")
    val accountId = result.head
    val treshold = serverParameterService.getServerParam(JourneyConstants.checkTreshold);
    val successQuery = db.run{
      checkHistory.filter(_.account_id === accountId).filter(_.check_result > treshold.toFloat).size.result
    }
    val successResult = Await.result(successQuery, 4.seconds)
    val failedQuery = db.run {
      checkHistory.filter(_.account_id === accountId).filter(_.check_result < treshold.toFloat).size.result
    }
    val failedResult = Await.result(failedQuery, 4.seconds)
    val totalCheckQuery = db.run {
      checkHistory.filter(_.account_id === accountId).size.result
    }
    val totalCheckResult = Await.result(totalCheckQuery, 4.seconds)
    CalculateResponse(success =  successResult, numOfAction = totalCheckResult, failed = failedResult)
  }
}