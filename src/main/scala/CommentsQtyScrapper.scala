package org.whsv26.habr

import CommentsQtyScrapper.XPath

import cats.Applicative
import cats.effect.IO
import cats.effect.kernel.*
import cats.effect.syntax.temporal.*
import cats.syntax.applicative.*
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.monadError.*
import cats.syntax.option.*
import fs2.Stream
import io.github.bonigarcia.wdm.WebDriverManager
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.openqa.selenium.By.ByClassName
import org.openqa.selenium.chrome.{ChromeDriver, ChromeDriverLogLevel, ChromeDriverService, ChromeOptions}
import org.openqa.selenium.interactions.{Actions, PointerInput}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.*
import retry.RetryPolicies.*
import retry.Sleep
import retry.syntax.all.*

import java.io.{FileOutputStream, IOException}
import java.net.{SocketTimeoutException, URL}
import java.util
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.jdk.DurationConverters.*

class CommentsQtyScrapper[F[_]: Sleep](driver: Driver)(using F: Async[F]) {
  private def scrapCommentsQty(source: URL): F[CommentCount] =
    F.blocking(driver.get(source.toString)) >>
      scrapWebElement(7.seconds)
        .map(elem => elem.flatMap(_.getText.toIntOption))
        .map(qty => qty.getOrElse(0))
        .map(CommentCount)

  private def scrapWebElement(timeout: FiniteDuration): F[Option[WebElement]] =
    Stream.repeatEval(scroll >> findWebElement)
      .metered(300.millis)
      .collectFirst { case Some(elem) => elem }
      .compile
      .toList
      .map(_.headOption)
      .timeoutTo(timeout, None.pure)

  private def scroll: F[Unit] =
    F.blocking(driver.executeScript("window.scrollBy(0, 200)"))

  private def findWebElement: F[Option[WebElement]] =
    F.delay(driver.findElements(By.xpath(XPath)))
      .map(_.asScala.headOption)
}

object CommentsQtyScrapper {
  def apply[F[_]: Sleep: Async](source: URL): F[CommentCount] =
    DriverFactory.make.use { driver =>
      new CommentsQtyScrapper[F](driver)
        .scrapCommentsQty(source)
    }

  private val XPath = "/html/body/div[1]/div[1]/div[2]" +
    "/main/div/div/div/div[1]/div" +
    "/div[2]/div[1]/div[2]/div/div[2]/a[1]/span"
}
