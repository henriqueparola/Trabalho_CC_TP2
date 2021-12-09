package Security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.Arrays;


public class CreatePassword {
        public static byte[] generateHMAC(String sharedKey) {
            try {
                Mac hash = Mac.getInstance("HmacSHA1");
                SecretKeySpec key = new SecretKeySpec(sharedKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
                hash.init(key);

                return hash.doFinal();
            }
            catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }

        public static boolean isAuthenticated(String sharedKey, byte[] hmac) {
            try {
                Mac hash = Mac.getInstance("HmacSHA256");
                SecretKeySpec key = new SecretKeySpec(sharedKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
                hash.init(key);

                byte[] result = hash.doFinal();

                return Arrays.equals(hmac, result);
            }
            catch (NoSuchAlgorithmException  | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }


        public static byte[] createDigest(String sharedKey, byte[] data) {
            String sha1 = "";
            byte[] res = null;

            try {
                Mac hash = Mac.getInstance("HmacSHA1");
                SecretKeySpec key = new SecretKeySpec(sharedKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
                hash.init(key);
                hash.update(data);
                res = hash.doFinal();
            }

            catch (NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
            }

            return res;
        }


    public static boolean checkAuthenticated(String sharedKey, byte[] data, byte[] hash) {
        try {
            Mac sha1 = Mac.getInstance("HmacSHA1");
            SecretKeySpec key = new SecretKeySpec(sharedKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            sha1.init(key);
            sha1.update(data);
            byte[] result = sha1.doFinal();

            return Arrays.equals(hash, result);
        }
        catch (NoSuchAlgorithmException  | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

}

