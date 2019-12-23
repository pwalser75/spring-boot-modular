package ch.frostnova.project.common.service.persistence;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.IntStream.rangeClosed;

/**
 * Identity generator (128 bit alphanumeric strings)
 *
 * @author pwalser
 * @since 25.12.2017.
 */
public class IdGenerator implements IdentifierGenerator {

    /**
     * Class name, to be used as annotation value - must be a constant.
     */
    public final static String CLASS_NAME = "ch.frostnova.project.common.service.persistence.IdGenerator";

    private final static String ALPHABET = createAlphabet();
    private final static int ID_BIT_LENGTH = 128;

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return generateAlphaNumericKey(ID_BIT_LENGTH);
    }

    private static String generateAlphaNumericKey(int bits) {

        BigInteger value = new BigInteger(bits, ThreadLocalRandom.current());

        if (value.equals(BigInteger.ZERO)) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        BigInteger len = BigInteger.valueOf(ALPHABET.length());

        StringBuilder builder = new StringBuilder();
        while (!value.equals(BigInteger.ZERO)) {
            int index = value.mod(len).intValue();
            builder.insert(0, ALPHABET.charAt(index));
            value = value.divide(len);
        }

        return builder.toString();
    }

    private static String createAlphabet() {
        return Stream.of(rangeClosed('0', '9'), rangeClosed('A', 'Z'), rangeClosed('a', 'z'))
                .flatMap(IntStream::boxed)
                .map(i -> String.valueOf((char) i.intValue()))
                .collect(Collectors.joining());
    }
}