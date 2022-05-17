package org.whsv26.habr

import Scrapper.{parseCommentsQty, parseCommentsQtyOld}

import cats.effect.kernel.{Resource, Temporal}
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.applicative.*
import cats.syntax.functor.*
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import fs2.Stream
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import retry.Sleep.sleepUsingTemporal

import java.net.URL
import scala.jdk.CollectionConverters.*

object Main extends IOApp {
  override def run(args: List[String]) = {
    val processFeed = Feed[IO]("https://habr.com/ru/rss/news/?fl=ru".url)
      .drop(10)
      .take(2)
      .evalTap(IO.println)
      .evalMap(post => parseCommentsQty[IO](post.link))
      .evalTap(qty => IO.println(s"Parsed qty: $qty"))
      .compile
      .drain

    for {
      _ <- IO.delay(WebDriverManager.chromedriver.setup())
      _ <- processFeed
    } yield (ExitCode.Success)
  }
}
