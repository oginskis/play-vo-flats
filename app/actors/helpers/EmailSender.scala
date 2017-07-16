package actors.helpers

import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import model.b2c.Flat
import play.api.Configuration

/**
  * Created by oginskis on 01/01/2017.
  */
class EmailSender (configuration: Configuration) {

  val localizationHelper = new LocalizationHelper

  val props = new java.util.Properties()
  props.put(EmailSender.SmtpPropStartTls, "true")
  props.put(EmailSender.SmtpPropSmtpAuth, "true")
  props.put(EmailSender.SmtpPropSmtpHost, configuration.get[String](EmailSender.SmtpHost))
  props.put(EmailSender.SmtpPropSmtpPort, configuration.get[String](EmailSender.SmtpPort))
  val session = Session.getInstance(props,
    new javax.mail.Authenticator() {
      override protected def getPasswordAuthentication(): javax.mail.PasswordAuthentication = {
        return new PasswordAuthentication(configuration.get[String](EmailSender.SmtpUsername),
          configuration.get[String](EmailSender.SmtpPassword))
      }
    });

  def sendEmail(flat: Flat) = {
    val message = new MimeMessage(session)
    message.setText(views.html.Application.notification
      .render(flat,localizationHelper.getMessages(Language.EN))
      .body,"utf-8", "html")
    message.setFrom(new InternetAddress(configuration.get[String](EmailSender.SentFrom)))
    message.setSubject(flat.address.get+ ", "+flat.district.get+", "+flat.city.get+", " +flat.price.get+ " EUR")
    message.setRecipients(Message.RecipientType.TO,
      configuration.get[String](EmailSender.SendToList).split(",")
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
  val SmtpHost = "smtp.host"
  val SmtpPort = "smtp.port"
  val SmtpUsername = "smtp.username"
  val SmtpPassword = "smtp.password"
  val SendToList = "smtp.sendto"
  val SentFrom = "smtp.sentfrom"
  val SmtpPropStartTls = "mail.smtp.starttls.enable"
  val SmtpPropSmtpAuth = "mail.smtp.auth"
  val SmtpPropSmtpHost = "mail.smtp.host"
  val SmtpPropSmtpPort = "mail.smtp.port"
}