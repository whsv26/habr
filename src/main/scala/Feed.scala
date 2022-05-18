package org.whsv26.habr

import cats.effect.kernel.{Resource, Sync}
import cats.syntax.applicative.*
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import fs2.Stream

import java.net.URL
import scala.jdk.CollectionConverters.*

class Feed[F[_]](source: URL)(using F: Sync[F]) {
  def stream: Stream[F, Post] =
    Stream.resource(Resource.fromAutoCloseable(F.delay(new XmlReader(source))))
      .evalMap(reader => F.delay((new SyndFeedInput).build(reader)))
      .map(_.getEntries.iterator.asScala)
      .flatMap(Stream.fromIterator(_, 32))
      .through(feedItemParser)

  private def feedItemParser(entries: Stream[F, SyndEntry]): Stream[F, Post] =
    entries.evalMapFilter { entry =>
      val title = Option(entry.getTitle)
      val uri = Option(entry.getUri).map(new URL(_))
      val date = Option(entry.getPublishedDate)

      (title, uri, date).mapN(Post.apply).pure
    }
}

object Feed {
  def apply[F[_]: Sync](source: URL): Stream[F, Post] =
    new Feed(source).stream
}
