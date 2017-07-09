package actors.helpers

import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import model.b2c.Flat
import play.api.Configuration

/**
  * Created by oginskis on 01/01/2017.
  */
class EmailSender (configuration: Configuration) {

  val props = new java.util.Properties()
  props.put(EmailSender.SMTP_PROP_START_TLS, "true")
  props.put(EmailSender.SMTP_PROP_SMTP_AUTH, "true")
  props.put(EmailSender.SMTP_PROP_SMTP_HOST, configuration.underlying.getString(EmailSender.SMTP_HOST))
  props.put(EmailSender.SMTP_PROP_SMTP_PORT, configuration.underlying.getString(EmailSender.SMTP_PORT))
  val session = Session.getInstance(props,
    new javax.mail.Authenticator() {
      override protected def getPasswordAuthentication(): javax.mail.PasswordAuthentication = {
        return new PasswordAuthentication(configuration.underlying.getString(EmailSender.SMTP_USERNAME),
          configuration.underlying.getString(EmailSender.SMTP_PASSWORD))
      }
    });

  def sendEmail(flat: Flat) = {
    val message = new MimeMessage(session)
    message.setText(views.html.Application.notification.render(flat).body,"utf-8", "html")
    message.setFrom(new InternetAddress(configuration.get[String](EmailSender.SENT_FROM)))
    message.setSubject(flat.address.get+ ", "+flat.district.get+", "+flat.city.get+", " +flat.price.get+ " EUR")
    message.setRecipients(Message.RecipientType.TO,
      configuration.underlying.getString(EmailSender.SENT_TO_LIST).split(",")
        .map(email => {
          val address: Address = new InternetAddress(email)
          address
        }
        ).array
    )
    Transport.send(message)
  }
}

object EmailSender {
  val SMTP_HOST = "smtp.host"
  val SMTP_PORT = "smtp.port"
  val SMTP_USERNAME = "smtp.username"
  val SMTP_PASSWORD = "smtp.password"
  val SENT_TO_LIST = "smtp.sendto"
  val SENT_FROM = "smtp.sentfrom"

  val SMTP_PROP_START_TLS = "mail.smtp.starttls.enable"
  val SMTP_PROP_SMTP_AUTH = "mail.smtp.auth"
  val SMTP_PROP_SMTP_HOST = "mail.smtp.host"
  val SMTP_PROP_SMTP_PORT = "mail.smtp.port"
}