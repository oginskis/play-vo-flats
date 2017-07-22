package actors.helpers

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.FileUtils

import scala.util.Try

/**
  * Created by oginskis on 16/07/2017.
  */
class LocalizationHelper {

  val fileEn = new File("tmpen")
  FileUtils.copyInputStreamToFile(getClass.getClassLoader.getResourceAsStream("messages.en.conf"), fileEn)
  val configEn = ConfigFactory.parseFile(fileEn)

  val fileLv = new File("tmplv")
  FileUtils.copyInputStreamToFile(getClass.getClassLoader.getResourceAsStream("messages.lv.conf"), fileLv)
  val configLv = ConfigFactory.parseFile(fileLv)


  def getMessage(key:String, lang: Language.Value): Option[String] = {
    lang match {
      case Language.EN => {
        Try(Option(configEn.getString(key))).getOrElse(None)
      }
      case Language.LV => {
        Try(Option(configLv.getString(key))).getOrElse(None)
      }
    }
  }

  def getMessages(lang:Language.Value): Config = {
    lang match {
      case Language.EN => configEn
      case Language.LV => configLv
    }
  }
}


