package ch.frostnova.module1.service.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Search query tokenizer, allows for quoted tokens, accepts token separation by whitespace, comma, semicolon.
 *
 * @author pwalser
 */
public final class SearchQueryTokenizer {

    private final Predicate<Character> isSeparator = c -> Character.isWhitespace(c) || c == ',' || c == ';';

    private enum State {
        SCAN_FOR_VALUE,
        READING_UNQUOTED_VALUE,
        READING_SINGLE_QUOTED_VALUE,
        READING_DOUBLE_QUOTED_VALUE
    }

    private State state = State.SCAN_FOR_VALUE;

    private SearchQueryTokenizer() {

    }

    private List<String> parse(String query) {
        if (query == null) {
            return Collections.emptyList();
        }
        List<String> tokens = new LinkedList<>();
        StringBuilder buffer = new StringBuilder();

        Runnable consumeBuffer = () -> {
            if (buffer.length() > 0) {
                tokens.add(buffer.toString());
                buffer.setLength(0);
            }
        };

        for (char c : query.toCharArray()) {

            if (state == State.SCAN_FOR_VALUE) {
                if (c == '\'') {
                    state = State.READING_SINGLE_QUOTED_VALUE;
                } else if (c == '\"') {
                    state = State.READING_DOUBLE_QUOTED_VALUE;
                } else if (!isSeparator.test(c)) {
                    buffer.append(c);
                    state = State.READING_UNQUOTED_VALUE;
                }
            } else if (state == State.READING_UNQUOTED_VALUE) {
                if (isSeparator.test(c)) {
                    consumeBuffer.run();
                    state = State.SCAN_FOR_VALUE;
                } else {
                    buffer.append(c);
                }

            } else if (state == State.READING_SINGLE_QUOTED_VALUE) {
                if (c == '\'') {
                    consumeBuffer.run();
                    state = State.SCAN_FOR_VALUE;
                } else {
                    buffer.append(c);
                }

            } else if (state == State.READING_DOUBLE_QUOTED_VALUE) {
                if (c == '\"') {
                    consumeBuffer.run();
                    state = State.SCAN_FOR_VALUE;
                } else {
                    buffer.append(c);
                }
            }
        }

        consumeBuffer.run();
        return tokens;
    }

    public static List<String> tokenize(String query) {
        return new SearchQueryTokenizer().parse(query);
    }

}
