package com.taste.utils;


import cn.hutool.core.util.RandomUtil;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class PasswordEncoder {

    public static String encode(String password) {
        // Generate a salt string
        String salt = RandomUtil.randomString(20);
        // Encode password
        return encode(password,salt);
    }
    private static String encode(String password, String salt) {
        // Encode password
        return salt + "@" + DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
    }
    public static Boolean matches(String encodedPassword, String rawPassword) {
        if (encodedPassword == null || rawPassword == null) {
            return false;
        }
        if(!encodedPassword.contains("@")){
            throw new RuntimeException("The password format is not valid!");
        }
        String[] arr = encodedPassword.split("@");
        // Get the salt string
        String salt = arr[0];
        // Compare
        return encodedPassword.equals(encode(rawPassword, salt));
    }
}
