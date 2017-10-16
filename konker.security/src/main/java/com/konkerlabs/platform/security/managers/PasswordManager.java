package com.konkerlabs.platform.security.managers;

import com.konkerlabs.platform.security.crypto.BCrypt;
import com.konkerlabs.platform.security.exceptions.SecurityException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Optional;

public class PasswordManager {

    private static final Config CONFIG = ConfigFactory.load().getConfig("password");

    public static final String STORAGE_PATTERN_DELIMITER = "$";
    public static final String STORAGE_PATTERN = "{0}"+
            STORAGE_PATTERN_DELIMITER+"{1}" +
            STORAGE_PATTERN_DELIMITER+"{2,number,#}" +
            STORAGE_PATTERN_DELIMITER+"{3}" +
            STORAGE_PATTERN_DELIMITER+"{4}";

    // The following constants may be changed without breaking existing hashes.
    public static final String QUALIFIER_PBKDF2 = "PBKDF2WithHmac";
    public static final String QUALIFIER_BCRYPT = "Bcrypt";
    public static final String HASH_ALGORITHM = CONFIG.getString("hash.algorithm");
    public static final int SALT_BYTES = CONFIG.getInt("salt.size");
    public static final int HASH_BYTES = 32;
    public static final int ITERATIONS = CONFIG.getInt("iterations");

    private static final int HASHING_FUNCTION_INDEX = 0;
    private static final int ITERATION_INDEX = 2;
    private static final int SALT_INDEX = 3;
    private static final int PBKDF2_INDEX = 4;

    protected byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);

        return salt;
    }

    /**
     * Generates a random string for password encoding
     *
     * @param   size    generated password string length
     * @return  generated password
     */
    public String generateRandomPassword(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param   password    the password to hash
     * @return              a salted PBKDF2 hash of the password
     * @throws SecurityException
     */
    public String createHash(String password) throws SecurityException {
        return createHash(password.toCharArray());
    }


    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param   password    the password to hash
     * @param   iterations cicles to enforce
     * @return              a salted PBKDF2 hash of the password
     * @throws SecurityException
     */
    public String createHash(String password, Optional<Integer> iterations) throws SecurityException {
        return createHash(password.toCharArray(), iterations);
    }



    /**
     * Returns a salted PBKDF2 hash of the password.
     * @param   password    the password to hash
     * @param   iterations cicles to enforce
     * @return              a salted PBKDF2 hash of the password
     */
    public String createHash(char[] password, Optional<Integer> iterations) throws SecurityException {
        try {
            // Generate a random salt
            byte[] salt = generateSalt();

            // Hash the password
            byte[] hash = pbkdf2(
                    password,
                    salt,
                    iterations.isPresent() ? iterations.get() : ITERATIONS,
                    HASH_BYTES
            );

            return MessageFormat.format(STORAGE_PATTERN, QUALIFIER_PBKDF2,HASH_ALGORITHM,ITERATIONS,toBase64(salt),toBase64(hash));
        } catch (NoSuchAlgorithmException|InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param   password    the password to hash
     * @return              a salted PBKDF2 hash of the password
     */
    public String createHash(char[] password) throws SecurityException {
        return createHash(password, Optional.empty());
    }

    /**
     * Validates a password using a hash (PBKDF2 or BCrypt)
     *
     * @param   password    the password to check
     * @param   goodHash    the hash of the valid password
     * @return              true if the password is correct, false if not
     */
    public boolean validatePassword(String password, String goodHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        return validatePassword(password.toCharArray(), goodHash);
    }

    /**
     * Validates a password using a hash (PBKDF2 or BCrypt)
     *
     * @param   password    the password to check
     * @param   goodHash    the hash of the valid password
     * @return              true if the password is correct, false if not
     */
    public boolean validatePassword(char[] password, String goodHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
    	if ("userNotFoundPassword".equals(goodHash)) {
    		return false;
    		
    	} else {
    		// Decode the hash into its parameters
    		String[] params = goodHash.split("\\"+STORAGE_PATTERN_DELIMITER);

    		switch (params[HASHING_FUNCTION_INDEX]) {
                case QUALIFIER_PBKDF2:
                    return validatePBKDF2Password(params, password);
                case QUALIFIER_BCRYPT:
                    return validateBcryptPassword(goodHash, password);
                 default:
                     return false;
            }

    	}
    }

    private boolean validateBcryptPassword(String goodHash, char[] password) {
        // Remove qualifier
        goodHash = goodHash.substring(goodHash.indexOf("$"));
        return BCrypt.checkpw(new String(password), goodHash);
    }

    private boolean validatePBKDF2Password(String[] params, char[] password) throws InvalidKeySpecException, NoSuchAlgorithmException {

        int iterations = Integer.parseInt(params[ITERATION_INDEX]);
        byte[] salt = fromBase64(params[SALT_INDEX]);
        byte[] hash = fromBase64(params[PBKDF2_INDEX]);
        // Compute the hash of the provided password, using the same salt,
        // iteration count, and hash length
        byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
        // Compare the hashes in constant time. The password is correct if
        // both hashes match.
        return slowEquals(hash, testHash);

    }

    /**
     * Compares two byte arrays in length-constant time. This comparison method
     * is used so that password hashes cannot be extracted from an on-line
     * system using a timing attack and then attacked off-line.
     *
     * @param   a       the first byte array
     * @param   b       the second byte array
     * @return          true if both byte arrays are the same, false if not
     */
    private boolean slowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    /**
     *  Computes the PBKDF2 hash of a password.
     *
     * @param   password    the password to hash.
     * @param   salt        the salt
     * @param   iterations  the iteration count (slowness factor)
     * @param   bytes       the length of the hash to compute in bytes
     * @return              the PBDKF2 hash of the password
     */
    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(QUALIFIER_PBKDF2 +HASH_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param   hex         the hex string
     * @return              the hex string decoded into a byte array
     */
    private byte[] fromHex(String hex)
    {
        byte[] binary = new byte[hex.length() / 2];
        for(int i = 0; i < binary.length; i++)
        {
            binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return binary;
    }

    /**
     * Converts a string of base64 characters into a byte array.
     *
     * @param   base        the base64 string
     * @return              the base64 string decoded into a byte array
     */
    private byte[] fromBase64(String base)
    {
        return Base64.getDecoder().decode(base);
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param   array       the byte array to convert
     * @return              a length*2 character string encoding the byte array
     */
    private String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    /**
     * Converts a byte array into a base64 string.
     *
     * @param   array       the byte array to convert
     * @return              a string encoding the byte array
     */
    private String toBase64(byte[] array)
    {
        return Base64.getEncoder().encodeToString(array);
    }
}