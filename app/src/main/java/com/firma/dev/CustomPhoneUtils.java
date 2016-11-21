package com.firma.dev;

import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;/**
 * Created by Eskoria on 07/08/2015.
 */
public class CustomPhoneUtils {

        /*
         * Special characters
         *
         * (See "What is a phone number?" doc)
         * 'p' --- GSM pause character, same as comma
         * 'n' --- GSM wild character
         * 'w' --- GSM wait character
         */
        public static final char PAUSE = ',';
        public static final char WAIT = ';';
        public static final char WILD = 'N';

        /** True if c is ISO-LATIN characters 0-9, *, # , +, WILD */
        public final static boolean isDialable(char c) {
            return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD;
        }

        /**
         * Format a phone number.
         * <p>
         * If the given number doesn't have the country code, the phone will be
         * formatted to the default country's convention.
         *
         * @param phoneNumber
         *            the number to be formatted.
         * @param defaultCountryIso
         *            the ISO 3166-1 two letters country code whose convention will
         *            be used if the given number doesn't have the country code.
         * @return the formatted number, or null if the given number is not valid.
         *
         * @hide
         */
        public static String formatNumber(String phoneNumber, String defaultCountryIso) {
            // Do not attempt to format numbers that start with a hash or star
            // symbol.
            if (phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
                return phoneNumber;
            }

            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            String result = null;
            try {
                PhoneNumber pn = util.parseAndKeepRawInput(phoneNumber, defaultCountryIso);
                result = util.formatInOriginalFormat(pn, defaultCountryIso);
            } catch (NumberParseException e) {
            }
            return result;
        }

        /**
         * Format the phone number only if the given number hasn't been formatted.
         * <p>
         * The number which has only dailable character is treated as not being
         * formatted.
         *
         * @param phoneNumber
         *            the number to be formatted.
         * @param phoneNumberE164
         *            the E164 format number whose country code is used if the given
         *            phoneNumber doesn't have the country code.
         * @param defaultCountryIso
         *            the ISO 3166-1 two letters country code whose convention will
         *            be used if the phoneNumberE164 is null or invalid.
         * @return the formatted number if the given number has been formatted,
         *         otherwise, return the given number.
         *
         * @hide
         */
        public static String formatNumber(String phoneNumber, String phoneNumberE164, String defaultCountryIso) {
            int len = phoneNumber.length();
            for (int i = 0; i < len; i++) {
                if (!isDialable(phoneNumber.charAt(i))) {
                    return phoneNumber;
                }
            }
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            // Get the country code from phoneNumberE164
            if (phoneNumberE164 != null && phoneNumberE164.length() >= 2 && phoneNumberE164.charAt(0) == '+') {
                try {
                    PhoneNumber pn = util.parse(phoneNumberE164, defaultCountryIso);
                    String regionCode = util.getRegionCodeForNumber(pn);
                    if (!TextUtils.isEmpty(regionCode)) {
                        defaultCountryIso = regionCode;
                    }
                } catch (NumberParseException e) {
                }
            }
            String result = formatNumber(phoneNumber, defaultCountryIso);
            return result != null ? result : phoneNumber;
        }

}

