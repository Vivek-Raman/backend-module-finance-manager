create table if not exists finance_expense
(
    id uuid not null primary key default gen_random_uuid (),
    summary character varying not null,
    amount decimal not null,
    date timestamp
);

CREATE INDEX IF NOT EXISTS idx_expense_date ON finance_expense (date DESC);
