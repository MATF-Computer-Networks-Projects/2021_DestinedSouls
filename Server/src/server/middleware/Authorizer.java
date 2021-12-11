package server.middleware;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Authorizer {
    private static Cipher encryptor = null;
    private static Cipher decryptor = null;
    private static String algorithm = "SHA-256";
    private static String secret = "0ac0f797-c86b-43fd-8c17-84c1bc83725b";


    private Authorizer() {}

    private static void init(MessageDigest md) {
        try {
            md.update(secret.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec key = new SecretKeySpec(md.digest(), "AES");
            encryptor = Cipher.getInstance("AES");
            encryptor.init(Cipher.ENCRYPT_MODE, key);
            decryptor = Cipher.getInstance("AES");
            decryptor.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /*
    * Using preset algorithm (default SHA-256)
    */
    public static void load() {
        if(encryptor != null && decryptor != null)
            return;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            init(md);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /*
    * Setting global configuration
    */
    public static void load(String algorithm, String secret) throws NoSuchAlgorithmException {
        Authorizer.algorithm = algorithm;
        Authorizer.secret = secret;
        encryptor = null;
        decryptor = null;
        MessageDigest md = MessageDigest.getInstance(algorithm);
        init(md);
    }

    public static byte[] encrypt(String passphrase) {
        try {
            return encryptor.doFinal(passphrase.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(byte[] hash) throws IllegalBlockSizeException, BadPaddingException {
        return new String(decryptor.doFinal(hash), StandardCharsets.UTF_8);
    }
}
