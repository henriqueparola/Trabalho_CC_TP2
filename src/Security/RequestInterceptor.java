package Security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RequestInterceptor {
    private String authorizationProvider;
    private String accessKey;
    private String secretKey;
    private List<String> headers;

    public static String calculateHMacHash(String key, String data) throws  Exception {
        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256.init(secretKey);

        return byteArrayToHex(sha256.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String byteArrayToHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for(byte b: array)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }


    /*
    public static void main(String [] args) throws Exception {

        // HMAC_SHA256("key", "The quick brown fox jumps over the lazy dog") = f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8
        System.out.println(calculateHMacHash("key", "The quick brown fox jumps over the lazy dog"));
    }
    */




}
