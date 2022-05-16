package org.whsv26

import java.net.URL

package object habr {
  extension (v: String)
    def url: URL = new URL(v)

  opaque type CommentCount = Int

  val CommentCount: Int => CommentCount = identity

  extension (v: CommentCount)
    def toInt: Int = v
}
