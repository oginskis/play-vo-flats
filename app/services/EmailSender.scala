package services

import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}

import model.Flat

import play.api.Configuration
import repo.FlatRepo

/**
  * Created by oginskis on 01/01/2017.
  */
@Singleton
class EmailSender @Inject() (configuration: Configuration, flatRepo: FlatRepo){

  val SMTP_HOST = "smtp.host"
  val SMTP_PORT = "smtp.port"
  val SMTP_USERNAME = "smtp.username"
  val SMTP_PASSWORD = "smtp.password"
  val SENT_TO_LIST = "smtp.sendto"
  val SENT_FROM = "smtp.sentfrom"
  val props = new java.util.Properties()
  props.put("mail.smtp.starttls.enable", "true")
  props.put("mail.smtp.auth", "true")
  props.put("mail.smtp.host", configuration.underlying.getString(SMTP_HOST))
  props.put("mail.smtp.port", configuration.underlying.getString(SMTP_PORT))

  val session = Session.getInstance(props,
    new javax.mail.Authenticator() {
      override protected def getPasswordAuthentication(): javax.mail.PasswordAuthentication = {
        return new PasswordAuthentication(configuration.underlying.getString(SMTP_USERNAME),
          configuration.underlying.getString(SMTP_PASSWORD))
      }
    });

  def sendEmail(flat: Flat) = {
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(configuration.underlying.getString(SENT_FROM)))
      message.setRecipients(Message.RecipientType.TO,
        configuration.underlying.getString(SENT_TO_LIST).split(",")
          .map(email=> {
            val address: Address = new InternetAddress(email)
            address
          }
          ).array
      )
      var historicFlatStr = flatRepo.findHistoricAdds(new Flat(flat.address,flat.rooms,flat.size,flat.floor))
        .filterNot(incomingFlat=>(incomingFlat.link == flat.link && incomingFlat.price == flat.price))
        .sortBy(flat=>flat.firstSeenAt)
        .map(flat=>flat.price.get+" EUR - Active between "+new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(flat.firstSeenAt.get)+
          " and "+new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(flat.lastSeenAt.get))
        .mkString("<br />")
      if (historicFlatStr.isEmpty){
        historicFlatStr = "Nothing has been found"
      }
      val textPart = new MimeBodyPart()
      textPart.setContent("<html><head></head><body>New flat posted or updated:"
        + "<br />"
        + "<br /><b>Address:</b> " + flat.address.get
        + "<br /><b>Floor:</b> " + flat.floor.get
        + "<br /><b>Rooms:</b> " + flat.rooms.get
        + "<br /><b>Size:</b> " + flat.size.get
        + "<br /><b>Price:</b> " + flat.price.get + " EUR"
        + "<br /><b>Link:</b> http://www.ss.lv" + flat.link.get
        + "<br />"
        + "<br /><b>Historic prices (for flats with the same address, floor, " +
        "number of rooms and size):</b> <br />"
        + historicFlatStr
        + "<br />"
        +  "<br />--Viktors</body></html>", "text/html; charset=UTF-8")
      message.setSubject(flat.address.get+ ", "+flat.price.get+" EUR")
      val mp = new MimeMultipart()
      mp.addBodyPart(textPart)
      message.setContent(mp)
      Transport.send(message)
    } catch {
      case ex:Exception =>
        throw new RuntimeException(ex);
    }
  }
}
