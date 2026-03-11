package com.lotorojo.plugandchat.tenant.util;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int KEY_LENGTH_BYTES = 32;

    public static String generateApiKey(){

        byte[] bytes = new byte[KEY_LENGTH_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }


}
