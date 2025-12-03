create table processed_tickets (
        id uuid primary key,
        title varchar(255) not null,
        description text,
        category varchar(50) not null,
        status varchar(50) not null,
        created_at timestamptz not null,
        priority varchar(50) not null,
        sla_hours int not null,
        processed_at timestamptz not null
                               );
