package ch.frostnova.module1.service.persistence;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Identity generator (128 bit alphanumeric strings)
 *
 * @author pwalser
 * @since 25.12.2017.
 */
public class IdGenerator implements IdentifierGenerator {

    private final static String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final static int ID_BIT_LENGTH = 128;


    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return generateAlphaNumericKey(ID_BIT_LENGTH);
    }

    private String generateAlphaNumericKey(int bits) {

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
}