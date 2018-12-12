package com.payline.payment.p24.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SecurityManager {
    private static final String MD_5 = "MD5";

    /**
     * Create a string from args and hash them with MD5 algorithm
     *
     * @param args every fields who have to be concatenated the hashed
     * @return the hash message
     */
    public String hash(String... args) {
        try {
            // create string to hash form args
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (String arg : args) {
                sb.append(arg);
                if (i++ < args.length - 1) {
                    sb.append("|");
                }
            }

            // do the hash
            MessageDigest md = MessageDigest.getInstance(MD_5);
            byte[] byteChain = sb.toString().getBytes(StandardCharsets.UTF_8);
            byte[] hash = md.digest(byteChain);

            // convert byte[] result into readable String
            BigInteger bigInt = new BigInteger(1, hash);
            return bigInt.toString(16);

        } catch (NoSuchAlgorithmException e) {
            // unreachable statement, this method can't return an error
            return null;
        }
    }
}
