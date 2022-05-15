package org.whsv26

import java.net.URL

package object habr {
  extension (s: String)
    def url: URL = new URL(s)
}
