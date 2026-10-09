package com.rashik.rashikmart.util;

import java.util.Locale;

/**
 * Validation helpers for seller-uploaded product images.
 *
 * <p>Only raster image formats that browsers render as inert images are
 * accepted. {@code .svg} is deliberately excluded because an uploaded SVG can
 * embed JavaScript and would execute in the application's origin when served
 * back to a browser (stored XSS).</p>
 */
public final class UploadUtil {

    private UploadUtil() {
        // Utility class
    }

    /**
     * @param fileName the originally submitted file name (may contain a path)
     * @return true only for the small allow-list of safe raster extensions
     */
    public static boolean isAllowedImageExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT).trim();
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".webp");
    }
}
