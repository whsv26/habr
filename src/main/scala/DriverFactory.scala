package org.whsv26.habr

import cats.effect.kernel.{Resource, Sync}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeDriverService, ChromeOptions}

import java.io.FileOutputStream

object DriverFactory {
  def make[F[_]: Sync]: Resource[F, Driver] = {
    val acquire = Sync[F].delay {

      val service = ChromeDriverService.Builder()
        .withSilent(true)
        .build()

      service.sendOutputTo(new FileOutputStream("/dev/null"))

      val args = Seq(
        "--no-sandbox",
        "--headless",
        "--disable-gpu",
        "--disable-crash-reporter",
        "--disable-extensions",
        "--disable-in-process-stack-traces",
        "--disable-dev-shm-usage",
      )

      new ChromeDriver(
        service,
        new ChromeOptions().addArguments(args: _*)
      )
    }

    Resource.make(acquire)(d => Sync[F].blocking(d.quit()))
  }
}
