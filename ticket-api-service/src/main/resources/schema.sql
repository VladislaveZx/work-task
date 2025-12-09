create table tickets (
                         id uuid primary key default gen_random_uuid(),
                         title varchar(255) not null,
                         description text,
                         category varchar(50) not null,
                         status varchar(50) not null,
                         created_at timestamp not null
)