package Security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.Arrays;


public class CreatePassword {
        public static byte[] generateHMAC(String sharedKey) {
            try {
                Mac hash = Mac.getInstance("HmacSHA256");
                SecretKeySpec key = new SecretKeySpec(sharedKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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
                SecretKeySpec key = new SecretKeySpec(sharedKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                hash.init(key);

                byte[] result = hash.doFinal();

                return Arrays.equals(hmac, result);
            }
            catch (NoSuchAlgorithmException  | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }

}

