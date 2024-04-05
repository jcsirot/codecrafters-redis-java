package org.chelonix.redis.command;

public class PingCommand implements RedisCommand {

    @Override
    public RedisCommandType getType() {
        return RedisCommandType.PING;
    }

    PingCommand() {}
}
