package org.whsv26.habr

import Scrapper.parseCommentsQty

import cats.effect.kernel.Temporal
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.applicative.*
import cats.syntax.functor.*
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import retry.Sleep.sleepUsingTemporal
import fs2.Stream

import java.net.URL
import scala.jdk.CollectionConverters.*

object Main extends IOApp {
  override def run(args: List[String]) = {

    Feed[IO]("https://habr.com/ru/rss/news/?fl=ru".url)
      .take(1)
      .evalTap(IO.println)
      .evalMap(post => parseCommentsQty[IO](post.link))
      .flatMap(Stream.fromOption(_))
      .evalTap(IO.println)
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
