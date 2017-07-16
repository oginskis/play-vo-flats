package actors.helpers

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

/**
  * Created by oginskis on 16/07/2017.
  */
class LocalizationHelper {

  val configEn = ConfigFactory.parseFile(new File("conf/messages.en.conf"))
  val configLv = ConfigFactory.parseFile(new File("conf/messages.lv.conf"))

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


