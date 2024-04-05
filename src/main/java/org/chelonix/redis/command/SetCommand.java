package org.chelonix.redis.command;

import java.time.Duration;

public class SetCommand implements RedisCommand {

    private final String key;
    private final String value;
    private Duration duration;

    SetCommand(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public RedisCommandType getType() {
        return RedisCommandType.SET;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Duration getDuration() {
        return duration;
    }

    public boolean hasExpiry() {
        return duration != null;
    }

    void setDuration(Duration duration) {
        this.duration = duration;
    }
}
