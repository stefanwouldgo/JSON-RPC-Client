package com.sagesex

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.ws._
import com.ning.http.client.Realm
import scala.util._

class JsonRPCProxy(val urlstring: String, rpcuser:String, rpcpassword:String, timeout:Int, maxcalls:Int) {

  val service = WS.url(urlstring).withAuth(rpcuser,rpcpassword,Realm.AuthScheme.BASIC).withRequestTimeout(timeout)

  var gid = 111

  private var runningCalls = 0
  private var sleepingCalls = 0
  def getSleepingCalls(i:Int):Int = synchronized{
    sleepingCalls = sleepingCalls + i
    sleepingCalls
  }

  def getRunningCalls(i:Int):Int = synchronized{
    runningCalls = runningCalls + i
    runningCalls
  }


  def call(method: String, parameters: Any*): Future[JsValue] =
  {
      getSleepingCalls(+1)
      while(getRunningCalls(0) > maxcalls )
        {println("Sleeping: "+getSleepingCalls(0)); Thread.sleep(getSleepingCalls(0)*timeout)}
      getSleepingCalls(-1)
      val params =
        for (p <- parameters)
        yield p match{
          case e:String  => Json.toJson(e)
          case e:Int => Json.toJson(e)
          case e     => throw( new Exception("Invalid parameter type " + e + ": Expected Int or String"))
        }

      val values =
        Map("method" -> Json.toJson(method), "params" -> Json.toJson(params), "id" -> Json.toJson(gid))

      gid = gid + 1
      val jsonValues = Json.toJson(values)

      getRunningCalls(+1)

      val futureResponse = service.post(Json.stringify(jsonValues))
      (for (response <- futureResponse) yield{
        val JsonResponse = response.json
        getRunningCalls(-1)
        (JsonResponse \ "error").asOpt[String] match {
          case Some(errorString) => throw new Exception("Server Error Reply:" + errorString)
          case None => JsonResponse \ "result"
        }
      }).recoverWith {case e =>
        getRunningCalls(-1)
        println("Retry!" + e.getMessage )
        if (parameters.length == 0) call(method)
        else if (parameters.length == 1) call(method,parameters.head)
        else call(method,parameters)
      }

  }
}
