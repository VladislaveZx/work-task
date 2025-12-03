package ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.status;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;

@WritingConverter
public class TicketStatusWriteConverter implements Converter<TicketStatus, String> {

    @Override
    public String convert(TicketStatus source) {
        return source.name();
    }
}
