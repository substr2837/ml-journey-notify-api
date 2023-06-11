package service

import entity.ServerParameter
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.DurationInt

@Singleton
class ServerParameterService @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  protected val serverParameter = TableQuery[ServerParameter]

  def getValue(param: String): String = {
    val query = db.run {
      serverParameter.filter(_.key === param).map(_.value).to[List].result
    }
    val result = Await.result(query, 4.seconds).to
    if (result.isEmpty) return "no value"
    result.head
  }

  def setValue(param: String, value: String): Unit = {
    val existingServerParamId: Int = getServerParam(param);
    if (existingServerParamId > 0) {
      val existingParamValue = for {param <- serverParameter if param.id === existingServerParamId} yield param.value
      existingParamValue.update(value)
      return
    }
    val query = DBIO.seq(
      serverParameter += (param, value)
    )
    Await.result(db.run(query), 4.seconds);
  }

  def getServerParam(param: String): Int = {
    val query = db.run {
      serverParameter.filter(_.key === param).map(_.id).to[List].result
    }
    val result = Await.result(query, 4.seconds)
    if (result.isEmpty)
      throw new RuntimeException("Server Param not found")
    result.head
  }
}
