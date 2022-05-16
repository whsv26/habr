package org.whsv26.habr

import java.net.URL
import java.util.Date

case class Post(
  title: String,
  link: URL,
  publishedAt: Date,
)
