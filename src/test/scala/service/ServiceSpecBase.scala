package service

import model.Profile._
import profile.simple._
import util.ControlUtil._
import util.DatabaseConfig
import java.sql.DriverManager
import org.apache.commons.io.FileUtils
import scala.util.Random
import java.io.File

trait ServiceSpecBase {

  def withTestDB[A](action: (Session) => A): A = {
    util.FileUtil.withTmpDir(new File(FileUtils.getTempDirectory(), Random.alphanumeric.take(10).mkString)){ dir =>
      val (url, user, pass) = (DatabaseConfig.url(Some(dir.toString)), DatabaseConfig.user, DatabaseConfig.password)
      org.h2.Driver.load()
      using(DriverManager.getConnection(url, user, pass)){ conn =>
        servlet.AutoUpdate.versions.reverse.foreach(_.update(conn))
      }
      Database.forURL(url, user, pass).withSession { session =>
        action(session)
      }
    }
  }

}
