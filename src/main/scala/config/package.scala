package org.whsv26.habr

import pureconfig.ConfigReader

package object config {
  case class Config(
    telegram: TelegramConfig,
  )

  object Config {
    given ConfigReader[Config] =
      ConfigReader.forProduct1("telegram")(Config.apply)
  }

  case class TelegramConfig(
    token: String,
    chat: String,
  )

  object TelegramConfig {
    given ConfigReader[TelegramConfig] =
      ConfigReader.forProduct2("token", "chat")(TelegramConfig.apply)
  }
}
