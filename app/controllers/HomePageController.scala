package controllers

import entity.CalculateResponse
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import service.HomePageService

import javax.inject.{Inject, Singleton}

@Singleton
class HomePageController @Inject()(val controllerComponents: ControllerComponents, val homePageService: HomePageService) extends BaseController {
  case class CalculateRequest(address: String)
  implicit val calculateRequestJson: OFormat[CalculateRequest] = Json.format[CalculateRequest];
  implicit val calculateResponseJson: OFormat[CalculateResponse] = Json.format[CalculateResponse]
  def calculate(): Action[AnyContent] = Action {
    implicit request =>
      val content = request.body.asJson
      val calculateRequest: Option[CalculateRequest] = content.flatMap(Json.fromJson[CalculateRequest](_).asOpt)
      Ok(Json.toJson(homePageService.calculatLatestActivity(calculateRequest.orNull.address)))
  }
}
