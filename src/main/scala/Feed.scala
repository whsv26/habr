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
  def stream: Stream[F, FeedItem] =
    Stream.resource(Resource.fromAutoCloseable(F.delay(new XmlReader(source))))
      .evalMap(reader => F.delay((new SyndFeedInput).build(reader)))
      .map(_.getEntries.iterator.asScala)
      .flatMap(Stream.fromIterator(_, 32))
      .evalMapFilter(entry => extractFeedItem(entry).pure)

  private def extractFeedItem(e: SyndEntry): Option[FeedItem] = {
    val title = Option(e.getTitle)
    val uri = Option(e.getUri).map(_.url)
    val date = Option(e.getPublishedDate)

    (title, uri, date).mapN(FeedItem.apply)
  }
}

object Feed {
  def apply[F[_]: Sync](source: URL): Stream[F, FeedItem] =
    new Feed(source).stream
}
