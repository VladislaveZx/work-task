package ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.category;

import org.springframework.core.convert.converter.Converter;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketCategory;

public class TicketCategoryWriteConverter implements Converter<TicketCategory, String> {

    @Override
    public String convert(TicketCategory source) {
        return source.name();
    }
}
