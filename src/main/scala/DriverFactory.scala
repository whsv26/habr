package org.whsv26.habr

import cats.effect.kernel.{Resource, Sync}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeDriverService, ChromeOptions}

import java.io.FileOutputStream

object DriverFactory {
  def make[F[_]: Sync]: Resource[F, Driver] =
    Resource.make(Sync[F].delay {
      val service = ChromeDriverService.Builder()
        .withSilent(true)
        .build()

      service.sendOutputTo(new FileOutputStream("/dev/null"))

      val options = new ChromeOptions()
      new ChromeDriver(service, options.addArguments(
        "--no-sandbox",
        "--headless",
        "--disable-gpu",
        "--disable-crash-reporter",
        "--disable-extensions",
        "--disable-in-process-stack-traces",
        "--disable-dev-shm-usage",
        // "--silent",
        // "--disable-logging",
        // "--log-level=3",
        // "--output=/dev/null",
      ))
    })(d => Sync[F].delay(d.quit()))
}
