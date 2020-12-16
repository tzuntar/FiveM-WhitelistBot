package com.redcreator37.WhitelistBot.Database;

import com.redcreator37.WhitelistBot.Localizations;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Contains methods related to password operations
 */
public class PasswordHandling {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a new random salt
     *
     * @param length the length of the salt
     * @return an {@link Optional} containing the salt
     */
    public static Optional<String> generateSalt(final int length) {
        if (length < 1) throw new IllegalArgumentException(Localizations
                .lc("salt-length-must-be-positive"));
        byte[] salt = new byte[length];
        RANDOM.nextBytes(salt);
        return Optional.of(Base64.getEncoder().encodeToString(salt));
    }

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 512;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    /**
     * Hashes this password combined with this salt
     *
     * @param password the password to hash
     * @param salt     the salt to use when hashing
     * @return the hashed password
     */
    public static Optional<String> hashPassword(String password, String salt) {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt.getBytes(), ITERATIONS, KEY_LENGTH);
        Arrays.fill(chars, Character.MIN_VALUE);
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] securePassword = secretKeyFactory.generateSecret(spec).getEncoded();
            return Optional.of(Base64.getEncoder().encodeToString(securePassword));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println(MessageFormat.format(Localizations
                    .lc("error-running-hash-function"), e.getMessage()));
            return Optional.empty();
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Matches the entered password against the password in the database
     *
     * @param entered the entered password
     * @param dbHash  the hash, retrieved from the database
     * @param salt    the salt used when hashing the initial password
     * @return whether the password matches or not
     */
    public static boolean passwordMatches(String entered, String dbHash, String salt) {
        Optional<String> optionalEncrypted = hashPassword(entered, salt);
        return optionalEncrypted.map(hash -> hash.equals(dbHash)).orElse(false);
    }

}
