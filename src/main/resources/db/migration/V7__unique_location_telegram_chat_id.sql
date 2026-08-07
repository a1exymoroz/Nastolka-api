ALTER TABLE locations ADD CONSTRAINT uq_locations_telegram_chat_id UNIQUE (telegram_chat_id);
