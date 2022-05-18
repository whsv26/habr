package org.whsv26.habr

import config.Config
import opaque.asInt

import cats.effect.kernel.{Resource, Temporal}
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.applicative.*
import cats.syntax.either.*
import cats.syntax.functor.*
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import fs2.Stream
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import pureconfig.*
import retry.Sleep.sleepUsingTemporal

import java.net.URL
import java.util.logging.{Level, Logger}
import scala.jdk.CollectionConverters.*

object Main extends IOApp {

  val RSS = "https://habr.com/ru/rss/news/?fl=ru"

  override def run(args: List[String]) = {
    for {
      config <- IO.defer(loadConfig)
      _ <- IO.delay(WebDriverManager.chromedriver.setup())
      _ <- IO.delay(System.setProperty("webdriver.chrome.silentOutput", "true"))
      _ <- IO.delay(Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF))
      _ <- TelegramBot[IO](config.telegram).use(processFeed)
    } yield ExitCode.Success
  }

  private def processFeed(bot: TelegramBot[IO]) =
    Feed[IO](new URL(RSS))
      .parEvalMap(5) { post =>
        CommentsQtyScrapper[IO](post.link)
          .map(PostWithComments(post, _))
      }
      .filter(_.comments.asInt > 5)
      .evalTap(IO.println)
      .evalTap(bot.send)
      .compile
      .drain

  private def loadConfig =
    IO.fromEither {
      ConfigSource
        .resources("app.conf")
        .load[Config]
        .leftMap(e => new RuntimeException(e.toString))
    }
}
