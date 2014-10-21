package models

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import play.Play

trait DBSupport {

  // ソートの逆順用
  def Desc[T: Ordering] = implicitly[Ordering[T]].reverse

  // DBへの接続を補助
  def connectDB[T](f: => T): T = {
    Database.forURL(Play.application().configuration().getString("db.default.url"), driver = "org.h2.Driver", user = "sa") withSession {
      f
    }
  }

}