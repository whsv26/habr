package org.whsv26.habr

import cats.Applicative
import cats.effect.kernel.Sync
import cats.syntax.applicative.*
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.monadError.*
import com.gargoylesoftware.htmlunit.{BrowserVersion, Page, WebClient}
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import retry.RetryPolicies.*
import retry.Sleep
import retry.syntax.all.*

import java.io.IOException
import java.net.{SocketTimeoutException, URL}
import java.util
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

object Scrapper {

  private val CommentCountClass = ".tm-comments-wrapper__comments-count"

  def parseCommentsQty[F[_]: Sleep: Sync](source: URL): F[Option[CommentCount]] =
    for {
      page <- fetchPage(source)
      doc <- Sync[F].delay(Jsoup.parse(page.asXml()))
      elems = doc.select(CommentCountClass).asScala
      comments = elems.headOption.flatMap(_.text().toIntOption)
    } yield comments.map(CommentCount)

  private def fetchPage[F[_]: Sleep: Sync](source: URL): F[HtmlPage] =
    for {
      client <- Sync[F].delay(new WebClient(BrowserVersion.CHROME))
      page <- Sync[F].delay(client.getPage[HtmlPage](source))
        .retryingOnAllErrors(retryPolicy, (_, _) => ().pure)
    } yield page

  private def retryPolicy[F[_]: Applicative] =
    limitRetries[F](0) join exponentialBackoff[F](10.second)

}
