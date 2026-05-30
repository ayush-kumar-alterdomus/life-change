ALTER TABLE users ADD COLUMN fcm_token VARCHAR(512);
ALTER TABLE users ADD COLUMN notification_preferences JSONB DEFAULT '{"enabled": true, "quietHoursStart": null, "quietHoursEnd": null, "types": {}}';
