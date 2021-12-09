package com.ptc.sendgrid.service.impl;

import com.ptc.sendgrid.domain.Account;
import com.ptc.sendgrid.domain.EmailMessage;
import com.ptc.sendgrid.service.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.ClickTrackingSetting;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.FooterSetting;
import com.sendgrid.helpers.mail.objects.MailSettings;
import com.sendgrid.helpers.mail.objects.OpenTrackingSetting;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.sendgrid.helpers.mail.objects.Setting;
import com.sendgrid.helpers.mail.objects.SubscriptionTrackingSetting;
import com.sendgrid.helpers.mail.objects.TrackingSettings;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SendGridEmailServiceImpl implements EmailService {

  @Autowired
  private SendGrid sendGrid;

  @Value("${app.sendgrid.tracking-setting.substitution-tag}")
  private String substitutionTag;

  private static final String SEND_EMAIL_URL = "mail/send";

  @Override
  public void sendText(final EmailMessage emailMessage) {
    try {
      sendEmail(emailMessage, "text/plain");
    } catch (IOException exception) {
      if (log.isErrorEnabled()) {
        log.error(
            "Can not send the email [{}], cause [{}]",
            emailMessage.toString(),
            ExceptionUtils.getStackTrace(exception));
      }
    }
  }

  @Override
  public void sendHTML(final EmailMessage emailMessage) {
    try {
      sendEmail(emailMessage, "text/html");
    } catch (IOException exception) {
      if (log.isErrorEnabled()) {
        log.error(
            "Can not send the email [{}], cause [{}]",
            emailMessage.toString(),
            ExceptionUtils.getStackTrace(exception));
      }
    }
  }

  private void sendEmail(final EmailMessage emailMessage, final String contentType) throws IOException {

    if (CollectionUtils.isNotEmpty(emailMessage.getToEmails())) {
      final Mail mail = createMail(emailMessage, contentType);

      final Request request = new Request();
      request.setMethod(Method.POST);
      request.setEndpoint(SEND_EMAIL_URL);
      request.setBody(mail.build());

      final Response response = sendGrid.api(request);
      if (log.isInfoEnabled()) {
        log.info("Status code: [{}], body: [{}], headers: [{}]",
            response.getStatusCode(),
            response.getBody(),
            response.getHeaders());
      }
    }
  }

  private Mail createMail(final EmailMessage emailMessage, final String contentType) {
    //TODO: validate email unique in bbc,cc,to

    final Mail mail = new Mail();

    final Personalization personalization = new Personalization();

    this.addPersonalizationToEmails(emailMessage.getToEmails(), personalization);
    this.addPersonalizationBccEmails(emailMessage.getBccEmails(), personalization);
    this.addPersonalizationCcEmails(emailMessage.getCcEmails(), personalization);

    mail.addPersonalization(personalization);
    mail.setFrom(this.createEmail(emailMessage.getFrom().getEmail(), emailMessage.getFrom().getName()));
    mail.setReplyTo(this.createEmail(emailMessage.getFrom().getEmail(), emailMessage.getFrom().getName()));
    if (StringUtils.isNotBlank(emailMessage.getSubject())) {
      mail.setSubject(emailMessage.getSubject());
    }

    this.setMailContent(mail, contentType, emailMessage.getBody());

//    final String attachmentContent = "PCFET0NUWVBFIGh0bWw+CjxodG1sIGxhbmc9ImVuIj4KCiAgICA8aGVhZD4KICAgICAgICA8bWV0YSBjaGFyc2V0PSJVVEYtOCI+CiAgICAgICAgPG1ldGEgaHR0cC1lcXVpdj0iWC1VQS1Db21wYXRpYmxlIiBjb250ZW50PSJJRT1lZGdlIj4KICAgICAgICA8bWV0YSBuYW1lPSJ2aWV3cG9ydCIgY29udGVudD0id2lkdGg9ZGV2aWNlLXdpZHRoLCBpbml0aWFsLXNjYWxlPTEuMCI+CiAgICAgICAgPHRpdGxlPkRvY3VtZW50PC90aXRsZT4KICAgIDwvaGVhZD4KCiAgICA8Ym9keT4KCiAgICA8L2JvZHk+Cgo8L2h0bWw+Cg==";
//    final String fileName = "index.html";
//    final String attachmentType = "text/html";
//    final String disposition = "attachment";
//    this.addAttachment(mail, attachmentContent, fileName, attachmentType, disposition);

    this.setMailSetting(mail);
    this.setMailTrackingSetting(mail);

    return mail;
  }

  private Email createEmail(final String email, final String name) {
    if (StringUtils.isNotBlank(name)) {
      return new Email(email, name);
    }
    return new Email(email);
  }

  private void addPersonalizationToEmails(
      final List<Account> accounts,
      final Personalization personalization) {
    if (CollectionUtils.isNotEmpty(accounts)) {
      accounts.forEach(account -> personalization
          .addTo(this.createEmail(account.getEmail(), account.getName())));
    }
  }

  private void addPersonalizationBccEmails(
      final List<Account> accounts,
      final Personalization personalization) {
    if (CollectionUtils.isNotEmpty(accounts)) {
      accounts.forEach(account -> personalization
          .addBcc(this.createEmail(account.getEmail(), account.getName())));
    }
  }

  private void addPersonalizationCcEmails(
      final List<Account> accounts,
      final Personalization personalization) {
    if (CollectionUtils.isNotEmpty(accounts)) {
      accounts.forEach(account -> personalization
          .addCc(this.createEmail(account.getEmail(), account.getName())));
    }
  }

  private void setMailContent(
      final Mail mail,
      final String contentType,
      final String contentValue) {
    final Content content = new Content();
    content.setType(contentType);
    content.setValue(contentValue);
    mail.addContent(content);
  }

//  private void addAttachment(
//      final Mail mail,
//      final String attachmentContent,
//      final String fileName,
//      final String attachmentType,
//      final String disposition) {
//    final Attachments attachment = new Attachments();
//    attachment.setContent(attachmentContent);
//    attachment.setFilename(fileName);
//    attachment.setType(attachmentType);
//    attachment.setDisposition(disposition);
//    mail.addAttachments(attachment);
//  }

  private void setMailSetting(final Mail mail) {
    final MailSettings mailSettings = new MailSettings();
    final Setting bypassListManagement = new Setting();
    bypassListManagement.setEnable(false);
    mailSettings.setBypassListManagement(bypassListManagement);
    final FooterSetting footerSetting = new FooterSetting();
    footerSetting.setEnable(false);
    mailSettings.setFooterSetting(footerSetting);
    final Setting sandBoxMode = new Setting();
    sandBoxMode.setEnable(false);
    mailSettings.setSandboxMode(sandBoxMode);
    mail.setMailSettings(mailSettings);
  }

  private void setMailTrackingSetting(final Mail mail) {
    final TrackingSettings trackingSettings = new TrackingSettings();
    final ClickTrackingSetting clickTrackingSetting = new ClickTrackingSetting();
    clickTrackingSetting.setEnable(true);
    clickTrackingSetting.setEnableText(false);
    trackingSettings.setClickTrackingSetting(clickTrackingSetting);
    final OpenTrackingSetting openTrackingSetting = new OpenTrackingSetting();
    openTrackingSetting.setEnable(true);
    openTrackingSetting.setSubstitutionTag(substitutionTag);
    trackingSettings.setOpenTrackingSetting(openTrackingSetting);
    final SubscriptionTrackingSetting subscriptionTrackingSetting = new SubscriptionTrackingSetting();
    subscriptionTrackingSetting.setEnable(false);
    trackingSettings.setSubscriptionTrackingSetting(subscriptionTrackingSetting);
    mail.setTrackingSettings(trackingSettings);
  }

}
