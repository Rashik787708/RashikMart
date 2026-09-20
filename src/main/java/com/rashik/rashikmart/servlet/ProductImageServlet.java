package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.config.DatabaseConfig;
import com.rashik.rashikmart.util.HtmlUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves product photos back to the browser.
 *
 * <p>Product uploads are stored in the EXTERNAL directory resolved by
 * {@link DatabaseConfig#getUploadDir()}, which lives outside the exploded
 * WAR so images survive re-deploys. Because the upload directory is no
 * longer under {@code WEB-INF/../images/products}, the container cannot
 * serve those files statically — this servlet reads them from the
 * configured directory and streams them to the client.</p>
 *
 * <p>Request form: {@code /images/products/<fileName>}.</p>
 *
 * <p>Resolution order for a single request:</p>
 * <ol>
 *     <li>The requested file, if it exists in the external upload
 *         directory.</li>
 *     <li>The same name inside the exploded WAR's
 *         {@code /images/products} (legacy images uploaded before the WAR
 *         layout change), if present.</li>
 *     <li>The bundled {@code /images/default-product.svg} fallback.</li>
 * </ol>
 *
 * <p>Security: only the final path segment is used (never an arbitrary
 * path), the name must be in a small allow-list of characters, and the
 * resolved external path is verified to stay inside the upload directory
 * before the file is opened — blocking path-traversal like
 * {@code ../..//etc/passwd}.</p>
 */
@WebServlet(name = "ProductImageServlet", urlPatterns = {"/images/products/*"})
public class ProductImageServlet extends HttpServlet {

    private static final String WAR_FALLBACK_BASE =
            "/images/products";

    private static final String DEFAULT_IMAGE =
            "/images/default-product.svg";

    private static final String SAFE_NAME_REGEX =
            "^[A-Za-z0-9][A-Za-z0-9._-]{0,200}$";

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String name = extractSafeFileName(request);

        if (name == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid file name"
            );

            return;
        }

        // 1. External upload directory
        Path externalFile =
                resolveInUploadDir(name);

        if (externalFile != null) {

            serveFile(externalFile, response);
            return;
        }

        // 2. Legacy location inside the exploded WAR
        if (serveWarResource(name, response)) {
            return;
        }

        // 3. Bundled default image
        try (InputStream in =
                     getServletContext()
                             .getResourceAsStream(DEFAULT_IMAGE)) {

            if (in != null) {
                sendStream(
                        response,
                        in,
                        contentTypeFor(name)
                );
                return;
            }
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Image not found"
        );
    }

    /**
     * Returns only the final path segment and ONLY if it conforms to the
     * strict name allow-list. Any path separator, {@code ..} segment or
     * unusual character yields null (and therefore a 400).
     */
    private static String extractSafeFileName(
            HttpServletRequest request
    ) {

        String pathInfo =
                request.getPathInfo();

        if (pathInfo == null
                || pathInfo.isEmpty()) {

            return null;
        }

        String raw =
                pathInfo.startsWith("/")
                        ? pathInfo.substring(1)
                        : pathInfo;

        int lastSlash =
                raw.lastIndexOf('/');

        String name =
                lastSlash >= 0
                        ? raw.substring(lastSlash + 1)
                        : raw;

        if (name == null
                || name.isEmpty()
                || name.contains("..")
                || name.contains("/")
                || name.contains("\\")) {

            return null;
        }

        if (!name.matches(SAFE_NAME_REGEX)) {
            return null;
        }

        return name;
    }

    /**
     * Verifies the final target stays within the configured upload
     * directory and returns it as an absolute Path, or null if the file
     * does not exist / the path cannot be trusted.
     */
    private static Path resolveInUploadDir(
            String name
    ) {

        File base =
                new File(
                        DatabaseConfig.getUploadDir()
                );

        if (!base.isAbsolute()) {

            base =
                    base.getAbsoluteFile();
        }

        File candidate =
                new File(base, name);

        try {

            Path basePath =
                    base.toPath().toRealPath();

            // A missing parent means the file is not there yet; treat as
            // not-found rather than an error.
            Path candidatePath =
                    candidate.toPath();

            if (!Files.exists(candidatePath)) {
                return null;
            }

            Path realCandidate =
                    candidatePath.toRealPath();

            if (!realCandidate
                    .startsWith(basePath)) {

                return null;
            }

            return realCandidate;

        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Tries to locate a legacy image with the same name inside the WAR's
     * /images/products directory Používame classpath-relative resource so
     * we remain agnostic to whether the app is running exploded or from a
     * WAR. Returns true if the resource existed and was streamed.
     */
    private boolean serveWarResource(
            String name,
            HttpServletResponse response
    ) throws IOException {

        String resource =
                WAR_FALLBACK_BASE
                        + "/"
                        + name;

        try (InputStream in =
                     getServletContext()
                             .getResourceAsStream(resource)) {

            if (in == null) {
                return false;
            }

            sendStream(
                    response,
                    in,
                    contentTypeFor(name)
            );

            return true;
        }
    }

    private static void serveFile(
            Path file,
            HttpServletResponse response
    ) throws IOException {

        sendStream(
                response,
                Files.newInputStream(file),
                contentTypeFor(
                        file.getFileName().toString()
                )
        );
    }

    private static void sendStream(
            HttpServletResponse response,
            InputStream in,
            String contentType
    ) throws IOException {

        response.setContentType(contentType);

        byte[] buffer = new byte[8192];
        int read;

        while ((read = in.read(buffer)) != -1) {
            response.getOutputStream().write(buffer, 0, read);
        }

        response.flushBuffer();
    }

    private static String contentTypeFor(
            String fileName
    ) {

        String lower =
                fileName.toLowerCase();

        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")) {

            return "image/jpeg";
        }

        if (lower.endsWith(".png")) {
            return "image/png";
        }

        if (lower.endsWith(".webp")) {
            return "image/webp";
        }

        if (lower.endsWith(".gif")) {
            return "image/gif";
        }

        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }

        return "application/octet-stream";
    }
}
