package com.taste.utils;

import cn.hutool.core.util.StrUtil;

/**
 * @author RolaZhang
 */
public class RegexUtils {
    /**
     * Check if the phone number format is valid
     * @param phone The phone number to be validated
     * @return true: valid ，false：not valid
     */
    public static boolean isPhoneInvalid(String phone){
        return mismatch(phone, RegexPatterns.PHONE_REGEX);
    }
    /**
     * Check if the Email format is valid
     * @param email The Email to be validated
     * @return true: valid ，false：not valid
     */
    public static boolean isEmailInvalid(String email){
        return mismatch(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * Check if the phone code format is valid
     * @param code The phone code to be validated
     * @return true: valid ，false：not valid
     */
    public static boolean isCodeInvalid(String code){
        return mismatch(code, RegexPatterns.VERIFY_CODE_REGEX);
    }

    // Validate if it matches the regex format
    private static boolean mismatch(String str, String regex){
        if (StrUtil.isBlank(str)) {
            return true;
        }
        return !str.matches(regex);
    }
}
