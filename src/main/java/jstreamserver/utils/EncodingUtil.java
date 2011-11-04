package jstreamserver.utils;

import java.io.UnsupportedEncodingException;

/**
 * Helper class which helps dealing with String encoding
 *
 * @author Sergey Prilukin
 */
public final class EncodingUtil {

    public static final String UTF8_ENCODING = "UTF-8";

    public static String encodeString(String string, String sourceCharset, String targetCharset) {
        try {
            return new String(string.getBytes(sourceCharset), targetCharset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeStringToUTF8(String string, String sourceCharset) {
        return encodeString(string, sourceCharset, UTF8_ENCODING);
    }

    public static String encodeStringFromUTF8(String string, String targetCharset) {
        return encodeString(string, UTF8_ENCODING, targetCharset);
    }

}
