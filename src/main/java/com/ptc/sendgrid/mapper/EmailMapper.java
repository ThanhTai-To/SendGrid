package com.ptc.sendgrid.mapper;


import com.ptc.sendgrid.api.gen.model.SendEmailRequest;
import com.ptc.sendgrid.domain.EmailMessage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmailMapper {

  EmailMapper INSTANCE = Mappers.getMapper(EmailMapper.class);

  EmailMessage toEmailMessage(SendEmailRequest sendEmailRequest);


}
