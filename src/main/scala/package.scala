package org.whsv26

import org.openqa.selenium.{JavascriptExecutor, WebDriver}

import java.net.URL
import java.util.Date
import scala.annotation.targetName

package object habr {
  type Driver = WebDriver with JavascriptExecutor

  extension (v: String)
    def url: URL = new URL(v)

  opaque type CommentCount = Int

  val CommentCount: Int => CommentCount = identity

  extension (v: CommentCount)
    def asInt: Int = v

  case class Post(
    title: String,
    link: URL,
    publishedAt: Date,
  )

  case class PostWithComments(
    title: String,
    link: URL,
    publishedAt: Date,
    comments: CommentCount
  )

  object PostWithComments {
    def apply(post: Post, comments: CommentCount): PostWithComments =
      PostWithComments(post.title, post.link, post.publishedAt, comments)
  }
}
