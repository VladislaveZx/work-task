package ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.status;


import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;

@ReadingConverter
public class TicketStatusReadConverter implements Converter<String, TicketStatus> {
    @Override
    public TicketStatus convert(String source) {
        return TicketStatus.valueOf(source);
    }
}
