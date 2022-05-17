package org.whsv26.habr

import cats.Applicative
import cats.effect.IO
import cats.effect.kernel.{Async, Concurrent, GenConcurrent, GenTemporal, Resource, Sync}
import cats.effect.syntax.temporal.*
import cats.syntax.applicative.*
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.option.*
import cats.syntax.monadError.*
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import retry.RetryPolicies.*
import retry.Sleep
import retry.syntax.all.*
import fs2.Stream

import java.io.IOException
import java.net.{SocketTimeoutException, URL}
import java.util
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.jdk.DurationConverters.*
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.{By, JavascriptExecutor, Keys, WebDriver, WebElement}
import org.openqa.selenium.By.ByClassName
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.interactions.{Actions, PointerInput}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}

object Scrapper {

  private val XPath =
    "/html/body/div[1]/div[1]/div[2]/main/div/div/div/div[1]/div/div[2]/div[1]/div[2]/div/div[2]/a[1]/span"

  private val Selector = By.xpath(XPath)

  def parseCommentsQtyOld[F[_]: Sleep: Sync](source: URL, driver: WebDriver with JavascriptExecutor): F[CommentCount] = {
    Sync[F].delay {
      driver.get(source.toString)
      driver.executeScript("window.scrollBy(0, 1000)")

      val wait = new WebDriverWait(driver, 100.seconds.toJava)
      val elem = wait.until(ExpectedConditions.presenceOfElementLocated(Selector))

      val qty = elem.getText.toIntOption.getOrElse(0)
      CommentCount(qty)
    }
  }

  def parseCommentsQty[F[_]: Sleep: Async](source: URL): F[CommentCount] =
    mkDriver.use { driver =>
      for {
        _ <- Sync[F].delay(driver.get(source.toString))
        scroll = Sync[F].delay(driver.executeScript("window.scrollBy(0, 200)"))
        elem <- Stream.repeatEval(scroll >> findElement(driver))
          .metered(300.millis)
          .collectFirst { case Some(elem) => elem }
          .compile
          .toList
          .map(_.headOption)
          .timeout(100.seconds)
          .handleError(_ => None)
      } yield CommentCount(elem.flatMap(_.getText.toIntOption).getOrElse(0))
    }

  def findElement[F[_]: Sleep: Sync](driver: WebDriver): F[Option[WebElement]] = {
    Sync[F].delay(driver.findElement(By.xpath(XPath)))
      .attempt
      .map(_.toOption)
  }

  def mkDriver[F[_]: Sync]: Resource[F, WebDriver with JavascriptExecutor] =
    Resource.make(Sync[F].delay {
      val options = new ChromeOptions()
      options.setHeadless(true)
      new ChromeDriver(options)
    })(d => Sync[F].delay(d.quit()))
}
