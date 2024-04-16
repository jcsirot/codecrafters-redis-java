package org.chelonix.redis.command;

public class InfoCommand implements RedisCommand {

    private final String section;

    @Override
    public RedisCommandType getType() {
        return RedisCommandType.INFO;
    }

    InfoCommand(String section) {
        this.section = section;
    }

    public String getSection() {
        return section;
    }
}
