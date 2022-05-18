package org.whsv26.habr

import config.TelegramConfig

import cats.Applicative
import cats.effect.kernel.{Async, Resource, Sync}
import cats.syntax.applicative.*
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.implicits.*

import scala.concurrent.duration.Duration


class TelegramBot[F[_]: Applicative](client: Client[F], config: TelegramConfig) {
  def send(post: PostWithComments): F[Unit] = {
    val telegramApiUri = uri"https://api.telegram.org" /
      s"bot${config.token}" /
      "sendMessage"

    val msg = Seq("Статья", "Комментариев", "Ссылка")
      .zip(Seq(post.title, post.comments, post.link))
      .map { case (k, v) => s"$k: $v" }
      .mkString("\n")

    val uri: Uri = telegramApiUri
      .withQueryParam("chat_id", config.chat)
      .withQueryParam("text", msg)

    client.get(uri)(_ => ().pure[F])
  }
}

object TelegramBot {
  def apply[F[_]: Async](config: TelegramConfig): Resource[F, TelegramBot[F]] =
    BlazeClientBuilder[F].resource
      .evalMap { client =>
        Sync[F].delay(new TelegramBot(client, config))
      }
}
