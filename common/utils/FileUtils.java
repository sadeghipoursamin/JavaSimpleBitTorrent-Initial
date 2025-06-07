package common.utils;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;

public class FileUtils {

	public static Map<String, String> listFilesInFolder(String folderPath) {
		Map<String, String> files = new HashMap<>();

		File folder = new File(folderPath);

		if (!folder.exists() || !folder.isDirectory()) {
			return files;
		}
		File[] fileList = folder.listFiles();
		if (fileList == null) {
			return files;
		}
		for (File file : fileList) {
			if (file.isFile()) {
				String hash = MD5Hash.HashFile(file.getAbsolutePath());
				if (hash != null) {
					files.put(hash, file.getName());
				}
			}
		}

		return files;
	}

	public static String getSortedFileList(Map<String, String> files) {
		if (files == null || files.isEmpty()) {
			return "Repository is empty.";
		}

		List<String> keys = new ArrayList<>(files.keySet());
		Collections.sort(keys);

		StringBuilder stringBuilder = new StringBuilder();
		for (String key : keys) {
			if (!stringBuilder.isEmpty()) {
				stringBuilder.append("\n");
			}
			stringBuilder.append(key);
		}

		return stringBuilder.toString();
	}

}
