package org.chelonix.redis.command;

public class GetCommand implements RedisCommand {

    private final String key;
    public GetCommand(String key) {
        this.key = key;
    }

    @Override
    public RedisCommandType getType() {
        return RedisCommandType.GET;
    }

    public String getKey() {
        return key;
    }
}
