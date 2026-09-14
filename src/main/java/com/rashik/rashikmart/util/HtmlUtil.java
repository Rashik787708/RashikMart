package com.rashik.rashikmart.util;

public class HtmlUtil {

    private HtmlUtil() {
        // Utility class
    }

    /**
     * Safely escapes HTML special characters to prevent Cross-Site Scripting (XSS).
     *
     * @param input Raw user-supplied string
     * @return Escaped string safe for HTML rendering, or empty string if null
     */
    public static String escape(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#x27;");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }
}
