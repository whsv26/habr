package org.whsv26.habr

import org.openqa.selenium.By

object Selectors {
  val CommentsCounter =
    By.xpath(
      "//div[@class='tm-article-presenter__body']" +
      "//span[@class='tm-article-comments-counter-link__value']"
    )
}
