package common.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hash {
	public static String HashFile(String filePath)  {
		try (FileInputStream fileInputStream = new FileInputStream(filePath)){
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");

			byte[] buffer = new byte[8192];
			int length;
			while ((length = fileInputStream.read(buffer)) != -1) {
				messageDigest.update(buffer, 0, length);
			}
			byte[] digest = messageDigest.digest();

			StringBuilder stringBuilder = new StringBuilder();
            for (byte b : digest) {
                stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

			return stringBuilder.toString();
		} catch (IOException e) {
			System.err.println("Error while reading file " + filePath + ": " + e.getMessage());
			return null;
		} catch (NoSuchAlgorithmException e) {
			System.err.println("MD5 algorithm not available: " + e.getMessage());
			return null;
		}
    }
}
