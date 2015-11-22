package com.herzog.android.zxing.encode;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

/**
 * This class does the work of decoding the user's request and extracting all
 * the data to be encoded in a barcode.
 * 
 */
final public class QRCodeEncoder {

	//private static final String TAG = QRCodeEncoder.class.getSimpleName();

	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

	/**
	 * use this function to encode data as a QR code in bitmap format
	 * 
	 * @param data in text format to be encoded
	 * @param dimension for the resulting bitmap
	 * @return a bitmap
	 * 
	 */
	public static Bitmap encodeAsBitmap(final String data, final int dimension) throws WriterException {
		
		// the data to encode is a param now
		//String data = intent.getStringExtra(KEY_ENCODE_DATA);
		
		String contentsToEncode = data;
		if (contentsToEncode == null) {
			return null;
		}
		Map<EncodeHintType, Object> hints = null;
		String encoding = guessAppropriateEncoding(contentsToEncode);
		if (encoding != null) {
			hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			hints.put(EncodeHintType.CHARACTER_SET, encoding);
		}
		BitMatrix result;
		try {
			// always in QR code format
			result = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE,
					dimension, dimension, hints);
		} catch (IllegalArgumentException iae) {
			// Unsupported format
			return null;
		}
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * helper 
	 * 
	 * @param contents
	 * @return
	 */
	private static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}

}
