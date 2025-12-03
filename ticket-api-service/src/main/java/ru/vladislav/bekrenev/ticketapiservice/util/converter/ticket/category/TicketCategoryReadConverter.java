package ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.category;

import org.springframework.core.convert.converter.Converter;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketCategory;

public class TicketCategoryReadConverter implements Converter<String, TicketCategory> {

    @Override
    public TicketCategory convert(String source) {
        return TicketCategory.valueOf(source);
    }
}
