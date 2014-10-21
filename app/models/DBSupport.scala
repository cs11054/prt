package models

//import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

trait DBSupport {

  // ソートの逆順用
  def Desc[T: Ordering] = implicitly[Ordering[T]].reverse

  val heroku = false

  val DB_URL = if (!heroku) "jdbc:h2:tcp://localhost:9093/db"
  else "postgres://zcgmsoybywazlx:HQt5HBNiYxgDE4LdQ337Ry_urb@ec2-54-83-204-78.compute-1.amazonaws.com:5432/dcn5isfl4oitng"
  val DRIVER = if (!heroku) "org.h2.Driver" else "org.postgresql.Driver"
  val USER = if (!heroku) "sa" else " 	zcgmsoybywazlx"
  val PASS = if (!heroku) "" else "HQt5HBNiYxgDE4LdQ337Ry_urb"

  // DBへの接続を補助
  def connectDB[T](f: => T): T = {
    Database.forURL(DB_URL, driver = DRIVER, user = USER, password = PASS) withSession {
      f
    }
  }

}