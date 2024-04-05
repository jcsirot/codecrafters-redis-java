package org.chelonix.redis.resp;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;

public class RESPParser {

    private final BufferedReader reader;

    public RESPParser(BufferedReader reader) {
        this.reader = reader;
    }

    private String getNextToken() throws IOException {
        String nextToken = reader.readLine();
        if (nextToken == null) {
            throw new EOFException();
        }
        return nextToken;
    }

    public int consumeArrayStart() throws IOException {
        String nextToken = getNextToken();
        if (!RESPType.ARRAY.matches(nextToken)) {
            throw new IllegalArgumentException("Expected Array, found %s".formatted(nextToken.charAt(0)));
        }
        return Integer.parseInt(nextToken.substring(1));
    }

    public String consumeString() throws IOException {
        String nextToken = getNextToken();
        if (RESPType.SIMPLE_STRING.matches(nextToken)) {
            return nextToken.substring(1);
        } else if (RESPType.BULK_STRING.matches(nextToken)) {
            int len = Integer.parseInt(nextToken.substring(1));
            char[] blob = new char[len];
            reader.read(blob);
            reader.readLine(); // consume last \r\n
            return new String(blob);
        } else {
            throw new IllegalArgumentException("Expected String, found %s".formatted(nextToken.charAt(0)));
        }
    }

    public int consumeInteger() throws IOException {
        String nextToken = getNextToken();
        if (!RESPType.INTEGER.matches(nextToken)) {
            throw new IllegalArgumentException("Expected Integer, found %s".formatted(nextToken.charAt(0)));
        }
        return Integer.parseInt(nextToken.substring(1));
    }

    public String consumeSimpleString() throws IOException {
        String nextToken = getNextToken();
        if (!RESPType.SIMPLE_STRING.matches(nextToken)) {
            throw new IllegalArgumentException("Expected Simple String, found %s".formatted(nextToken.charAt(0)));
        }
        return nextToken.substring(1);
    }

    public String consumeBulkString() throws IOException {
        return new String(consumeBulkStringAsBytes(), StandardCharsets.UTF_8);
    }

    public byte[] consumeBulkStringAsBytes() throws IOException {
        String nextToken = getNextToken();
        if (!RESPType.BULK_STRING.matches(nextToken)) {
            throw new IllegalArgumentException("Expected Bulk String, found %s".formatted(nextToken.charAt(0)));
        }
        int len = Integer.parseInt(nextToken.substring(1));
        char[] blob = new char[len];
        reader.read(blob);
        reader.readLine(); // consume last \r\n
        return toBytes(blob);
    }

    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }
}
