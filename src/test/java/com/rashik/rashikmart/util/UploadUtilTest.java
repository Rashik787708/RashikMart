package com.rashik.rashikmart.util;

import org.junit.Assert;
import org.junit.Test;

public class UploadUtilTest {

    @Test
    public void testAllowsSafeRasterImageExtensions() {
        Assert.assertTrue(UploadUtil.isAllowedImageExtension("photo.jpg"));
        Assert.assertTrue(UploadUtil.isAllowedImageExtension("photo.JPEG"));
        Assert.assertTrue(UploadUtil.isAllowedImageExtension("image.png"));
        Assert.assertTrue(UploadUtil.isAllowedImageExtension("banner.webp"));
        Assert.assertTrue(UploadUtil.isAllowedImageExtension("/tmp/path/pic.PNG"));
    }

    @Test
    public void testRejectsSvgToPreventStoredXss() {
        Assert.assertFalse(UploadUtil.isAllowedImageExtension("evil.svg"));
        Assert.assertFalse(UploadUtil.isAllowedImageExtension("EVIL.SVG"));
        Assert.assertFalse(UploadUtil.isAllowedImageExtension("payload.svg"));
    }

    @Test
    public void testRejectsOtherAndMalformedExtensions() {
        Assert.assertFalse(UploadUtil.isAllowedImageExtension("script.js"));
        Assert.assertFalse(UploadUtil.isAllowedImageExtension("archive.gif"));
        Assert.assertFalse(UploadUtil.isAllowedImageExtension("noextension"));
        Assert.assertFalse(UploadUtil.isAllowedImageExtension("photo.jpg.exe"));
        Assert.assertFalse(UploadUtil.isAllowedImageExtension(null));
        Assert.assertFalse(UploadUtil.isAllowedImageExtension(""));
    }
}
