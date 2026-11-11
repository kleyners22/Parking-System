package com.parking.service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class QrService {
  /** Devuelve un PNG en Base64 (data URL) con el contenido indicado. */
  public String generateBase64Png(String content) {
    try {
      BitMatrix m = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 250, 250);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(m, "PNG", out);
      return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo generar el QR", e);
    }
  }
}
