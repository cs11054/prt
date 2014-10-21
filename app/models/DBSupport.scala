package models

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import play.Play

trait DBSupport {

  val DB_PATH = Play.application().configuration().getString("db.default.url")
  val DRIVER = if (DB_PATH.contains("localhost")) "org.h2.Driver" else "org.postgresql.Driver"
  val USER = if (DB_PATH.contains("localhost")) "sa" else "zcgmsoybywazlx"
  val PASS = if (DB_PATH.contains("localhost")) "" else "HQt5HBNiYxgDE4LdQ337Ry_urb"

  // ソートの逆順用
  def Desc[T: Ordering] = implicitly[Ordering[T]].reverse

  // DBへの接続を補助
  def connectDB[T](f: => T): T = {
    Database.forURL(DB_PATH, driver = DRIVER, user = USER, password = PASS) withSession {
      f
    }
  }

}