package org.whsv26.habr

import opaque.asInt

import cats.effect.kernel.{Resource, Temporal}
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.either.*
import cats.syntax.applicative.*
import cats.syntax.functor.*
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import fs2.Stream
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import org.whsv26.habr.config.Config
import pureconfig.ConfigSource
import retry.Sleep.sleepUsingTemporal

import java.net.URL
import java.util.logging.{Level, Logger}
import scala.jdk.CollectionConverters.*

import pureconfig._

object Main extends IOApp {

  val RSS = "https://habr.com/ru/rss/news/?fl=ru"

  override def run(args: List[String]) = {
    val processFeed = Feed[IO](new URL(RSS))
      .drop(11)
      .take(1)
      .parEvalMap(3) { post =>
        CommentsQtyScrapper[IO](post.link)
          .map(PostWithComments(post, _))
      }
      .filter(_.comments.asInt > 0)
      .evalTap(IO.println)
      .compile
      .drain

    for {
      config <- IO.fromEither {
        ConfigSource
          .resources("app.conf")
          .load[Config]
          .leftMap(e => new RuntimeException(e.toString))
      }
      _ <- IO.delay(WebDriverManager.chromedriver.setup())
      _ <- IO.delay(System.setProperty("webdriver.chrome.silentOutput", "true"))
      _ <- IO.delay(Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF))
      _ <- processFeed
    } yield (ExitCode.Success)
  }
}
