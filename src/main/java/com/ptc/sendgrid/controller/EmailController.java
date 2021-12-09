package com.ptc.sendgrid.controller;

import com.ptc.sendgrid.api.gen.EmailsApi;
import com.ptc.sendgrid.api.gen.model.SendEmailRequest;
import com.ptc.sendgrid.domain.EmailMessage;
import com.ptc.sendgrid.mapper.EmailMapper;
import com.ptc.sendgrid.service.EmailService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EmailController implements EmailsApi {

  private final EmailService emailService;

  private final FreeMarkerConfigurer freemarkerConfigurer;

  private static final EmailMapper EMAIL_MAPPER = EmailMapper.INSTANCE;

  @Override
  public ResponseEntity<Void> sendEmail(final SendEmailRequest sendEmailRequest) {
    final EmailMessage emailMessage = EMAIL_MAPPER.toEmailMessage(sendEmailRequest);
    try {
      emailMessage.setBody(getEmailContent(emailMessage));
      emailService.sendHTML(emailMessage);
    } catch (IOException exception) {
      if (log.isErrorEnabled()) {
        log.error("IOException: cause [{}].", ExceptionUtils.getStackTrace(exception));
      }
    } catch (TemplateException exception) {
      if (log.isErrorEnabled()) {
        log.error("TemplateException: cause [{}]", ExceptionUtils.getStackTrace(exception));
      }
    }
    return ResponseEntity.noContent().build();
  }

  //TODO: hard code here
  private String getEmailContent(final EmailMessage emailMessage)
      throws IOException, TemplateException {
    final Template freemarkerTemplate = freemarkerConfigurer.getConfiguration()
        .getTemplate("account-activation.ftlh");
    return FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerTemplate,
        this.getModel(emailMessage));
  }

  //TODO: hard code here
  private Map<String, Object> getModel(final EmailMessage emailMessage) {
    final Map<String, Object> model = new HashMap<>();
    model.put("email", emailMessage.getToEmails().get(0).getEmail());
    return model;
  }

}
