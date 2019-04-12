package reader;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

public class Encoder {

	public static void main(String[] args) {
		encoder();
	}

	private static void encoder() {
		
		String contents = "http://www.ieee-security.org/TC/SP2019/cfposters.html";
		
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // GBK
		BitMatrix matrix = null;

		try {
			matrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, 300, 300, hints);
		} catch (WriterException e) {
			e.printStackTrace();
		}

		File file = new File("correction.png");
		try {
			MatrixToImageWriter.writeToFile(matrix, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
