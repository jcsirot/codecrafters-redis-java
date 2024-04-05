package org.chelonix.redis.command;

import org.chelonix.redis.resp.RESPParser;

import java.io.EOFException;
import java.io.IOException;

public class RedisCommandDecoder {

    private final RESPParser respParser;

    public RedisCommandDecoder(RESPParser respParser) {
        this.respParser = respParser;
    }

    public RedisCommand decode() throws IOException {
        int len = 0;
        try {
            len = respParser.consumeArrayStart();
        } catch (EOFException eofe) {
            return null;
        }
        if (len <= 0) {
            throw new IllegalArgumentException("Empty command");
        }
        String commandName = respParser.consumeString();
        return switch (commandName.toUpperCase()) {
            case "PING" -> {
                if (len == 2) {
                    respParser.consumeString();
                }
                yield new PingCommand();
            }
            case "ECHO" -> {
                String message = respParser.consumeString();
                yield new EchoCommand(message);
            }
            case "SET" -> {
                String key = respParser.consumeString();
                String value = respParser.consumeString();
                yield new SetCommand(key, value);
            }
            case "GET" -> {
                String key = respParser.consumeString();
                yield new GetCommand(key);
            }
            default -> throw new UnsupportedOperationException("Unsupported command: " + commandName.toUpperCase());
        };
    }

}
