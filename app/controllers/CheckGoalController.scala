package controllers

import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import service.{CheckHistoryService, ServerParameterService}
@Singleton
class CheckGoalController @Inject()(val controllerComponents: ControllerComponents, val checkHistoryService: CheckHistoryService, val serverParemeterService: ServerParameterService) extends BaseController {
  case class CheckGoalRequest(userAction: String, realGoal: String, address: String)
  case class CheckGoalResponse(result: String)
  case class CheckResultRequest(request_no: String)

  implicit val checkGoalRequestJson: OFormat[CheckGoalRequest] = Json.format[CheckGoalRequest]
  implicit val checkGoalResponseJson: OFormat[CheckGoalResponse] = Json.format[CheckGoalResponse]
  implicit val checkResultRequestJson: OFormat[CheckResultRequest] = Json.format[CheckResultRequest]

  def check(): Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val checkRequest: Option[CheckGoalRequest] =
      jsonObject.flatMap(
        Json.fromJson[CheckGoalRequest](_).asOpt
      )
    val generateReqNo = CheckService.generateReqNo()
    new Thread(() => {
      val checkResult = CheckService.checkGoal(checkRequest.orNull.realGoal, checkRequest.orNull.userAction)
      var checkExistingId = checkHistoryService.checkAccountIsExist(checkRequest.orNull.address)
      if (checkExistingId == 0) {
        checkExistingId = checkHistoryService.createAccount(checkRequest.orNull.address)
      } else {
        checkHistoryService.modifyLatestAccess(checkExistingId)
      }
      checkHistoryService.createHistory(checkExistingId, checkRequest.orNull.realGoal, checkRequest.orNull.userAction, checkResult.toString, generateReqNo)
    }).start()
    val checkGoalResponse: CheckGoalResponse = CheckGoalResponse(result = generateReqNo)
    Ok(Json.toJson(checkGoalResponse))
  }

  def getCheckResult: Action[AnyContent] = Action {
    implicit request =>
      val jsonObject = request.body.asJson
      val checkResultRequest: Option[CheckResultRequest] = jsonObject.flatMap(Json.fromJson[CheckResultRequest](_).asOpt)
      val result = checkHistoryService.getCheckResult(checkResultRequest.orNull.request_no)
      val existingTreshold = serverParemeterService.getServerParam(JourneyConstants.checkTreshold)
      val checkGoalResponse: CheckGoalResponse = CheckGoalResponse(if (result.toInt > existingTreshold) "success" else "failed")
      Ok(Json.toJson(checkGoalResponse))
  }
}
