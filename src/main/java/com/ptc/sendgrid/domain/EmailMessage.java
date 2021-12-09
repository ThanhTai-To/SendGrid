package com.ptc.sendgrid.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EmailMessage {

  private Account from;

  private List<Account> toEmails;

  private List<Account> ccEmails;

  private List<Account> bccEmails;

  private String body;

  private String subject;

}
