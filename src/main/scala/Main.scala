package org.whsv26.habr

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}

import java.net.URL
import scala.jdk.CollectionConverters.*
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor.*

object Main extends IOApp {
  override def run(args: List[String]) = {
    Feed[IO]("https://habr.com/ru/rss/news/?fl=ru".url)
      .evalTap(IO.println)
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
