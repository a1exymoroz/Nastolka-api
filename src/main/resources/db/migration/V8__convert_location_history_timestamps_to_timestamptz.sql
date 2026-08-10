ALTER TABLE location_history
    ALTER COLUMN played_at TYPE TIMESTAMPTZ USING played_at AT TIME ZONE 'Europe/Warsaw',
    ALTER COLUMN started_at TYPE TIMESTAMPTZ USING started_at AT TIME ZONE 'Europe/Warsaw',
    ALTER COLUMN finished_at TYPE TIMESTAMPTZ USING finished_at AT TIME ZONE 'Europe/Warsaw';
