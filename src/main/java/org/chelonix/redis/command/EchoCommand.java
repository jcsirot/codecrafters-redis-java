package org.chelonix.redis.command;

public class EchoCommand implements RedisCommand {

    private final String message;

    @Override
    public RedisCommandType getType() {
        return RedisCommandType.ECHO;
    }

    EchoCommand(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
