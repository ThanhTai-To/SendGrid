package com.ptc.sendgrid.service;

import com.ptc.sendgrid.domain.EmailMessage;

public interface EmailService {

  void sendText(EmailMessage emailMessage);

  void sendHTML(EmailMessage emailMessage);

}
