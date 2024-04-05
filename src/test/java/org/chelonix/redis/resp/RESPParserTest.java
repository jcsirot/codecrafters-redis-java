package org.chelonix.redis.resp;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RESPParserTest {

    @Test
    public void should_decode_simple_string() throws IOException {
        String str = "+TOTO\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(str));
        RESPParser parser = new RESPParser(reader);
        String value = parser.consumeSimpleString();
        assertThat(value).isEqualTo("TOTO");
    }

    @Test
    public void should_decode_bulk_string() throws IOException {
        String str = "$4\r\nTOTO\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(str));
        RESPParser parser = new RESPParser(reader);
        String value = parser.consumeBulkString();
        assertThat(value).isEqualTo("TOTO");
    }

    @Test
    public void should_decode_bulk_string_with_CRLF() throws IOException {
        String str = "$10\r\nTATA\r\nTOTO\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(str));
        RESPParser parser = new RESPParser(reader);
        String value = parser.consumeBulkString();
        assertThat(value).isEqualTo("TATA\r\nTOTO");
    }
}