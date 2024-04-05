package org.chelonix.redis.resp;

public enum RESPType {
    SIMPLE_STRING('+'), ARRAY('*'), BULK_STRING('$'), INTEGER(':');

    private char prefix;

    RESPType(char prefix) {
        this.prefix = prefix;
    }

    public char prefix() {
        return prefix;
    }

    public boolean matches(String token) {
        return token.charAt(0) == prefix;
    }
}
