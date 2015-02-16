package service

import model.Profile._
import profile.simple._
import model.{WebHook, Account}
import org.slf4j.LoggerFactory
import service.RepositoryService.RepositoryInfo
import util.JGitUtil
import org.eclipse.jgit.diff.DiffEntry
import util.JGitUtil.CommitInfo
import org.eclipse.jgit.api.Git
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.NameValuePair

trait HubotWebHookService {
  import HubotWebHookService._

  private val logger = LoggerFactory.getLogger(classOf[HubotWebHookPayload])
  private val webHookUrl = "http://192.168.11.9:9000/gitbucket-webhook"

  def callHubotWebHook(payload: HubotWebHookPayload): Unit = {
    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization.{read, write}
    import org.apache.http.client.methods.HttpPost
    import org.apache.http.impl.client.HttpClientBuilder
    import scala.concurrent._
    import ExecutionContext.Implicits.global

    logger.debug("start callHubotWebHook")
    implicit val formats = Serialization.formats(NoTypeHints)

    if(webHookURLs.nonEmpty){
      val json = write(payload)
      val httpClient = HttpClientBuilder.create.build

      val f = Future {
        logger.debug(s"start web hook invocation for ${webHookUrl}")
        val httpPost = new HttpPost(webHookUrl)

        val params: java.util.List[NameValuePair] = new java.util.ArrayList()
        params.add(new BasicNameValuePair("payload", json))
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"))

        httpClient.execute(httpPost)
        httpPost.releaseConnection()
        logger.debug(s"end web hook invocation for ${webHookUrl}")
      }
      f.onSuccess {
        case s => logger.debug(s"Success: web hook request to ${webHookUrl}")
      }
      f.onFailure {
        case t => logger.error(s"Failed: web hook request to ${webHookUrl}", t)
      }
    }
    logger.debug("end callHubotWebHook")
  }

}

object HubotWebHookService {
  case class HubotWebHookPayload(
    owner: String,
    name: String,
    issueId: Int,
    content: String)
}
