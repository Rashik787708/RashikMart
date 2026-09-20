package com.rashik.rashikmart.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Per-session Cross-Site Request Forgery protection.
 *
 * <p>A cryptographically random token is stored in the session under the
 * attribute {@code csrfToken} and echoed back into every state-changing
 * form as a hidden field with the same name. On each POST, the servlet's
 * doPost() compares the token submitted by the browser with the token in
 * the session using a constant-time comparison. Mismatched or missing
 * tokens are rejected.</p>
 *
 * <p>THE TOKEN IS NEVER VALIDATED INSIDE A FILTER. Multipart POST bodies
 * are not fully parsed until request.getPart() is called, so a filter's
 * request.getParameter() would not see a token sent inside a multipart
 * body. Each individual servlet validates CSRF inside doPost() instead,
 * after the session/auth checks have already passed.</p>
 */
public final class CsrfUtil {

    private static final String SESSION_ATTR = "csrfToken";
    private static final String FIELD_NAME = "csrfToken";

    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    /**
     * Generates a fresh 32-byte random token, URL-safe base64 encoded
     * without padding.
     */
    public static String generateToken() {

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /**
     * Returns the CSRF token already stored on the session, creating and
     * storing a fresh one if the session has none yet.
     *
     * @param session the current HTTP session (must not be null and must
     *                already be authenticated)
     * @return the session CSRF token, never null
     */
    public static String getOrCreateToken(
            HttpSession session
    ) {

        Object existing =
                session.getAttribute(SESSION_ATTR);

        if (existing instanceof String
                && !((String) existing).isEmpty()) {

            return (String) existing;
        }

        String token = generateToken();
        session.setAttribute(
                SESSION_ATTR,
                token
        );

        return token;
    }

    /**
     * Extracts the submitted CSRF token from the request.
     *
     * <p>Because a multipart request body is not visible via
     * request.getParameter() until the parts have been parsed, we first
     * try to read the {@code csrfToken} part (which forces parsing of the
     * multipart body) and fall back to the normal request parameter for
     * regular (non-multipart) POST bodies.</p>
     *
     * @return the raw submitted token, or null if none was provided
     */
    public static String extractToken(
            HttpServletRequest request
    ) {

        try {

            Part part = request.getPart(FIELD_NAME);
            if (part != null) {

                String value =
                        new String(
                                part.getInputStream().readAllBytes(),
                                StandardCharsets.UTF_8
                        );

                if (value != null) {
                    return value.trim();
                }
            }

        } catch (Exception ignored) {
            // Some containers may not have parsed the multipart body yet;
            // fall through to the parameter path below.
        }

        String param = request.getParameter(FIELD_NAME);
        return param == null
                ? null
                : param.trim();
    }

    /**
     * Constant-time check of whether the submitted token matches the
     * token stored in the session.
     *
     * @param request the current request; its submitted {@code csrfToken}
     *                (part or parameter) is compared with the session token
     * @return true only when both tokens are present, non-empty and equal
     */
    public static boolean isValid(
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return false;
        }

        Object sessionToken =
                session.getAttribute(SESSION_ATTR);

        if (!(sessionToken instanceof String)) {
            return false;
        }

        String submitted =
                extractToken(request);

        if (submitted == null
                || submitted.isEmpty()) {

            return false;
        }

        return constantTimeEquals(
                (String) sessionToken,
                submitted
        );
    }

    /**
     * Compares two strings in constant time to defend against timing
     * side-channel attacks on the CSRF token.
     */
    public static boolean constantTimeEquals(
            String expected,
            String actual
    ) {

        if (expected == null
                || actual == null) {

            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}
