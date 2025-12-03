package ru.vladislav.bekrenev.ticketapiservice.config.r2dbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.category.TicketCategoryReadConverter;
import ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.category.TicketCategoryWriteConverter;
import ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.status.TicketStatusReadConverter;
import ru.vladislav.bekrenev.ticketapiservice.util.converter.ticket.status.TicketStatusWriteConverter;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return R2dbcCustomConversions.of(
                PostgresDialect.INSTANCE,
                new TicketCategoryReadConverter(),
                new TicketCategoryWriteConverter(),
                new TicketStatusReadConverter(),
                new TicketStatusWriteConverter()
        );
    }
}
