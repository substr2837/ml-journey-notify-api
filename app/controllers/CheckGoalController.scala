package controllers

import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import service.CheckProcessor
@Singleton
class CheckGoalController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  case class CheckGoalRequest(userAction: String, realGoal: String)
  case class CheckGoalResponse(result: Double, reduceAmount: Int)
  implicit val checkGoalRequestJson: OFormat[CheckGoalRequest] = Json.format[CheckGoalRequest]
  implicit val checkGoalResponseJson: OFormat[CheckGoalResponse] = Json.format[CheckGoalResponse]
  def check(): Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val checkRequest: Option[CheckGoalRequest] =
      jsonObject.flatMap(
        Json.fromJson[CheckGoalRequest](_).asOpt
      )
    val checkProcessor = CheckProcessor
    val result = checkProcessor.checkGoal(checkRequest.orNull.realGoal, checkRequest.orNull.userAction )
    val checkGoalResponse: CheckGoalResponse = CheckGoalResponse(result = result, reduceAmount = 100)
    Ok(Json.toJson(checkGoalResponse))
  }
}
