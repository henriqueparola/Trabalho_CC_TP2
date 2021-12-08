package Security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class RequestInterceptor {
    private String authorizationProvider;
    private String accessKey;
    private String secretKey;
    private List<String> headers;

    public static String calculateHMacHash(String key, String data) throws Exception {
        Mac sha1 = Mac.getInstance("HmacSHA1");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
        sha1.init(secretKey);
        return byteArrayToHex(sha1.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String byteArrayToHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for(byte b: array)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }



    public static void main(String [] args) throws Exception {

        // HMAC_SHA256("key", "The quick brown fox jumps over the lazy dog") = f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8
        System.out.println(calculateHMacHash("key", "The quick brown fox jumps over the lazy dog"));
    }

    public static String calculateChecksum(byte[] bytearray) {
        String sha1 = "";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.reset();
            messageDigest.update(bytearray);
            sha1 = String.format("%040x", new BigInteger(1, messageDigest.digest()));
        }

        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sha1;
    }





}
