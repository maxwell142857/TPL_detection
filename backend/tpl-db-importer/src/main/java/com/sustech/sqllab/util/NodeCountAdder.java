package com.sustech.sqllab.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class NodeCountAdder {

	@SuppressWarnings("DataFlowIssue")
	public static void main(String[] args) throws IOException{
		for (File group : new File("tpl-db-importer/src/main/resources/root").listFiles()) {
			for (File artifact : group.listFiles()) {
				for (File version : artifact.listFiles()) {
					List<String> lines = Files.readAllLines(version.toPath());
					FileWriter writer = new FileWriter(version);
					for (String line : lines) {
						writer.write(line+" 1\n");
					}
					writer.close();
				}
			}
		}
	}
}
