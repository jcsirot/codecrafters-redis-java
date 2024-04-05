package org.chelonix.redis.command;

public class SetCommand implements RedisCommand {

    private final String key;
    private final String value;

    public SetCommand(String key, String value) {
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
}
