package com.konkerlabs.platform.security.test;

import com.konkerlabs.platform.security.managers.PasswordManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PasswordManagerTest {

    private static final Config TEST_CONFIG = ConfigFactory.load().getConfig("password");

    private PasswordManager subject;
    private String password;
    private String qualifier;
    private String hashAlgorithm;
    private String iterations;

    private static byte[] TEST_SALT;

    @BeforeClass
    public static void beforeClass() {
        TEST_SALT = new byte[PasswordManager.HASH_BYTES];
        new SecureRandom().nextBytes(TEST_SALT);
    }

    @Before
    public void setUp() throws Exception {
        subject = new PasswordManager();

        password = subject.generateRandomPassword(12);

        qualifier = "PBKDF2WithHmac";
        hashAlgorithm = TEST_CONFIG.getString("hash.algorithm");
        iterations = TEST_CONFIG.getString("iterations");
    }

    private byte[] encode(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), TEST_SALT, Integer.valueOf(iterations), PasswordManager.HASH_BYTES * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(qualifier+hashAlgorithm);
        return skf.generateSecret(spec).getEncoded();
    }

    @Test
    public void validateHashFormat() throws Exception {

        subject = new PasswordManager() {
            @Override
            protected byte[] generateSalt() {
                return TEST_SALT;
            }
        };

        String hashed = subject.createHash(password);

        String[] splitted = hashed.split("\\"+PasswordManager.STORAGE_PATTERN_DELIMITER);

        assertThat(splitted,arrayWithSize(5));
        assertThat(splitted[0],equalTo(qualifier));
        assertThat(splitted[1],equalTo(hashAlgorithm));
        assertThat(splitted[2],equalTo(iterations));
        assertThat(splitted[3],equalTo(Base64.getEncoder().encodeToString(TEST_SALT)));
        assertThat(splitted[4],equalTo(Base64.getEncoder().encodeToString(encode(password))));
    }

    @Test
    public void shouldValidateIfTwoHashesForSamePasswordAreDifferent() throws Exception {
        String hash = subject.createHash(password);
        String secondHash = subject.createHash(password);

        assertThat(hash,not(equalTo(secondHash)));
    }

    @Test
    public void givenAValidHashItShouldInvalidateAWrongPassword() throws Exception {
        String hash = subject.createHash(password);
        String wrongPassword = password+"typo";

        assertThat(subject.validatePassword(wrongPassword,hash),is(false));
    }

    @Test
    public void givenAValidHashItShouldValidateAValidPassword() throws Exception {
        String hash = subject.createHash(password);

        assertThat(subject.validatePassword(password,hash),is(true));
    }
    
    @Test
    public void givenAnInvalidHashItShouldInvalidateAPassword() throws Exception {
        String hash = "userNotFoundPassword";

        assertThat(subject.validatePassword(password,hash),is(false));
    }
}