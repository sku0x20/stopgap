-- migrate:up
CREATE TABLE notes (
    note_id uuid PRIMARY KEY,
    text    text NOT NULL
);

-- migrate:down
DROP TABLE notes;
