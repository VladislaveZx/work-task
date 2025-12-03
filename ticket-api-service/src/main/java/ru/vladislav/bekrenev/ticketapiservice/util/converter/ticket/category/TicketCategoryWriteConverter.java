package ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.category;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketCategory;

@WritingConverter
public class TicketCategoryWriteConverter implements Converter<TicketCategory, String> {

    @Override
    public String convert(TicketCategory source) {
        return source.name();
    }
}
