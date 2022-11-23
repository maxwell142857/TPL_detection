package com.sustech.sqllab.util;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class FileTreeMaker {

	private static final String OLD_ROOT_PATH="tpl-db-importer/src/main/resources/database";
	private static final String NEW_ROOT_PATH="tpl-db-importer/src/main/resources/root";

	@SneakyThrows(IOException.class)
	@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
	public static void main(String[] args) {
		for (File file : new File(OLD_ROOT_PATH).listFiles()) {
			String[] split = file.getName().split("\\$");
			String groupName = split[0].replace('!','.');
			String artifactName = split[1].replace('!','.');
			String versionName = split[2].replace('!','.');
			String artifactPath = NEW_ROOT_PATH + "/" + groupName + "/" + artifactName;
			File artifactDir = new File(artifactPath);
			if(!artifactDir.exists()){
				artifactDir.mkdirs();
			}
			write(artifactPath+"/"+versionName,Files.readString(file.toPath()));
		}
	}

	@SneakyThrows(IOException.class)
	private static void write(String path, String content){
		@Cleanup FileWriter fileWriter = new FileWriter(path);
		fileWriter.write(content);
	}
}
