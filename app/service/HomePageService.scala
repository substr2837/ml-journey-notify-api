package service

import com.google.inject.Singleton
import entity.{Account, CalculateResponse, CheckHistory}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.ast.ScalaBaseType.intType
import slick.jdbc.JdbcProfile
import util.JourneyConstants

import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@Singleton
class HomePageService @Inject()(val dbConfigProvider: DatabaseConfigProvider, val serverParameterService: ServerParameterService) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val checkHistory = TableQuery[CheckHistory]
  val account = TableQuery[Account]

  def calculatLatestActivity(address: String): CalculateResponse = {
    val treshold = serverParameterService.getServerParam(JourneyConstants.checkTreshold)
    val accountIdQuery = db.run {
      account.filter(_.address.toString() == address).map(_.id).to[List].result
    }
    val accountId = Await.result(accountIdQuery, 4.seconds).head

    val failedCountQuery = db.run {
      checkHistory.filter(_.id === accountId).filter(_.check_result.toString().toInt < treshold).size.result
    }
    val failedCount = Await.result(failedCountQuery, 4.seconds)
    val successCountQuery = db.run {
      checkHistory.filter(_.id === accountId).filter(_.check_result.toString().toInt > treshold).size.result
    }
    val successCount = Await.result(successCountQuery, 4.seconds)
    val totalCheckQuery = db.run {
      checkHistory.filter(_.id === accountId).size.result
    }
    val totalCheck = Await.result(totalCheckQuery, 4.seconds)
    CalculateResponse(failed = failedCount, success = successCount, numOfAction = totalCheck)
  }
}
