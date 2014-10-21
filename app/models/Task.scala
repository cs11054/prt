package models

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import java.util.Date
import util.Utilities
import java.io.File
import scala.io.Source
import util.XMLConv

case class Task(subjectid: Int, userid: String, taskid: Int, caption: String, body: String, date: Long) {

  def formatDate(form: String = "yyyy/mm/dd hh:mm"): String = form match {
    case "yyyy/mm/dd hh:mm" => "%tY/%<tm/%<td %<tR" format new Date(date)
    case _ => date.toString()
  }

  def toXML = <subjectid>{ subjectid }</subjectid>
              <userid>{ userid }</userid>
              <taskid>{ taskid }</taskid>
              <caption>{ caption }</caption>
              <body>{ body }</body>
              <date>{ date }</date>
}

object Tasks extends Table[Task]("TASK") with DBSupport with XMLConv {

  def subjectid = column[Int]("SUBJECTID", O.PrimaryKey, O.NotNull)
  def userid = column[String]("USERID", O.PrimaryKey, O.NotNull)
  def taskid = column[Int]("TASKID", O.PrimaryKey, O.NotNull)
  def caption = column[String]("CAPTION")
  def body = column[String]("BODY")
  def date = column[Long]("DATE", O.NotNull)
  def * = subjectid ~ userid ~ taskid ~ caption ~ body ~ date <> (Task, Task.unapply _)
  def ins = subjectid ~ userid ~ taskid ~ caption ~ body ~ date

  val SAVE_NAME = "tasks.xml"

  def save(path: String) { writeXML(path + SAVE_NAME, all()) }

  def load(path: String) {
    val list = readXML(path + SAVE_NAME) { node =>
      val sid = (node \ "subjectid").text.toInt
      val uid = (node \ "userid").text
      val tid = (node \ "taskid").text.toInt
      val caption = (node \ "caption").text
      val body = (node \ "body").text
      val date = (node \ "date").text.toLong
      Task(sid, uid, tid, caption, body, date)
    }
    list.foreach(add(_))
  }

  def add(t: Task) = connectDB {
    if (!Query(Tasks).list().exists(x => x.subjectid == t.subjectid && x.userid == t.userid && x.taskid == t.taskid))
      Tasks.ins.insert(t.subjectid, t.userid, t.taskid, t.caption, t.body, t.date)
  }

  def all(): List[Task] = connectDB {
    Query(Tasks).sortBy(_.date).list
  }

  def allDel() = connectDB {
    Query(Tasks).delete
  }

  def tasksOfSbj(sid: Int): List[Task] = connectDB {
    Query(Tasks).filter(_.subjectid === sid).sortBy(_.date.desc).list
  }

  def sortedTasksOfSbj(sid: Int, key: String): List[Task] = {
    val tasks = tasksOfNoDupSbj(sid)
    key match {
      case "date" => tasks
      case "rdate" => tasks.reverse
      case "iine" => tasks.map(t => t -> Iines.countIine(sid, t.userid)).sortBy(_._2)(Desc).map(_._1)
      case "riine" => tasks.map(t => t -> Iines.countIine(sid, t.userid)).sortBy(_._2).map(_._1)
      case "cmt" => tasks.map(t => t -> Comments.countComment(sid, t.userid)).sortBy(_._2)(Desc).map(_._1)
      case "rcmt" => tasks.map(t => t -> Comments.countComment(sid, t.userid)).sortBy(_._2).map(_._1)
      case _ => tasks.sortBy(_.date)(Desc)
    }
  }

  def tasksOfNoDupSbj(sid: Int): List[Task] = connectDB {
    Query(Tasks).filter(_.subjectid === sid).list
      .groupBy(_.userid).map(_._2.maxBy(_.taskid)).toList.sortBy(_.date)(Desc)
  }

  def getTasks(sid: Int, uid: String): List[Task] = connectDB {
    Query(Tasks).filter(t => t.subjectid === sid && t.userid === uid).sortBy(_.taskid).list
  }

  def getCaptionAndCode(sid: Int, uid: String): List[(String, String)] = {
    getTasks(sid, uid).map(t => (t.caption, t.body))
  }

  def add(subjectid: Int, userid: String, caption: String, body: String): Int = connectDB {
    val nextid = Query(Tasks).filter(t => t.subjectid === subjectid && t.userid === userid).list.size + 1
    val date = System.currentTimeMillis()
    Tasks.ins.insert(subjectid, userid, nextid, caption, body, date)
    nextid
  }

  def postNTasks(id: String, limit: Int): List[Task] = connectDB {
    Query(Tasks).filter(t => t.userid === id).sortBy(_.date.desc).take(limit).list
  }

  def delete(subjectid: Int, userid: String, taskid: Int): Int = connectDB {
    Tasks.filter(t => t.subjectid === subjectid
      && t.userid === userid && t.taskid === taskid).delete
  }

  // ユーザーの投稿の中で一番新しい番号を返す、ないならば-1
  def newestNumOfUser(sid: Int, uid: String): Int = connectDB {
    Query(Tasks).filter(t => t.subjectid === sid && t.userid === uid).sortBy(_.date.desc)
      .list.headOption.map(_.taskid).getOrElse(-1)
  }

}
    
