package io.virinchi.ekart.Util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Iterator;

//Shared by DataInitializer (seed photos) and SellerController (real uploads).
//TiDB Cloud Serverless rejects any single row entry over 6MB - a hard,
//non-configurable limit - and a raw phone photo can easily be several MB
//once Base64-encoded. Every image that goes into Product.image is resized
//and recompressed here first, so that limit is never hit either way.
public final class ImageUtil {

    private static final int MAX_IMAGE_DIMENSION = 1000; //px, longest side
    private static final float JPEG_QUALITY = 0.8f;

    private ImageUtil() {
    }

    //Reads any InputStream (a ClassPathResource for seed data, or a
    //MultipartFile's stream for a real upload), resizes/recompresses it,
    //and returns the Base64 string ready to store in Product.image.
    //Returns null if the image can't be read (not a real image file, etc).
    public static String toCompressedBase64(InputStream in) throws IOException {
        BufferedImage original = ImageIO.read(in);
        if (original == null) {
            return null;
        }
        BufferedImage resized = resizeIfNeeded(original);
        byte[] jpegBytes = toCompressedJpeg(resized);
        return Base64.getEncoder().encodeToString(jpegBytes);
    }

    private static BufferedImage resizeIfNeeded(BufferedImage original) {
        int w = original.getWidth();
        int h = original.getHeight();
        if (w <= MAX_IMAGE_DIMENSION && h <= MAX_IMAGE_DIMENSION) {
            return original;
        }

        double scale = (double) MAX_IMAGE_DIMENSION / Math.max(w, h);
        int newW = (int) Math.round(w * scale);
        int newH = (int) Math.round(h * scale);

        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newW, newH, null);
        g.dispose();
        return resized;
    }

    private static byte[] toCompressedJpeg(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();
        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(JPEG_QUALITY);

        //JPEG has no alpha channel - drop it if present to avoid write errors
        BufferedImage rgb = image;
        if (image.getColorModel().hasAlpha()) {
            rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rgb, null, null), params);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

}
