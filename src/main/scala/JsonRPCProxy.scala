package com.sagesex

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch._

class JsonRPCProxy(val urlstring: String, rpcuser:String, rpcpassword:String) {

  val service = url(urlstring).as_!(rpcuser,rpcpassword)
  var gid = 111


  def call(method: String, parameters: String*): Future[Either[Throwable,JsValue]] =
  {
    val values = Map("method" -> Json.toJson(method), "params" -> Json.toJson(parameters), "id" -> Json.toJson(gid))
    gid = gid + 1
    val jsonValues = Json.toJson(values)

    def request = service << Json.stringify(jsonValues)
    val HttpResponse = Http(request OK as.String).either

    for (either <- HttpResponse)
      yield either match {
       case Right(string) => { val JsonResponse = Json.parse(string)
          (JsonResponse \ "error").asOpt[String] match {
            case Some(errorString) => Left(new Exception("Server Error Reply:" + errorString))
            case None => Right(JsonResponse \ "result")
          }
        }
       case Left(e) => Left(e)
       }
  }

}
