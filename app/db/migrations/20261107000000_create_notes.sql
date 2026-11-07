-- migrate:up
-- IF NOT EXISTS adopts databases created before migrations, when PostgresStore made the table itself.
CREATE TABLE IF NOT EXISTS notes (
    note_id uuid PRIMARY KEY,
    text    text NOT NULL
);

-- migrate:down
DROP TABLE notes;
