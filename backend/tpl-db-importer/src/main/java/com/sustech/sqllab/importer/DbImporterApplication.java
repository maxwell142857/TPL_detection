package com.sustech.sqllab.importer;

import com.sustech.sqllab.importer.dao.*;
import com.sustech.sqllab.importer.po.Fingerprint;
import com.sustech.sqllab.importer.po.Version;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

@SpringBootApplication
@MapperScan("com.sustech.sqllab.importer.dao")
public class DbImporterApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(DbImporterApplication.class, args);
	}

	@Value("${root-path}")
	private String rootPath;
	@Resource
	private VersionDao versionDao;
	@Resource
	private FingerprintDao fingerprintDao;
	@Override
	public void run(ApplicationArguments args) throws IOException {
		//noinspection ConstantConditions
		for (File versionFile : new File(rootPath).listFiles()) {
			String fileName = versionFile.getName();
			String name = fileName.substring(0, fileName.lastIndexOf('.'));
			System.out.println("Start "+name);
			Version version =Version.builder()
									.artifactId(0)
									.usage(0)
									.name(name)
									.build();
			versionDao.insert(version);
			for (String hash : new HashSet<>(Files.readAllLines(Path.of(versionFile.getAbsolutePath())))) {

				fingerprintDao.insert(new Fingerprint(hash,version.getId()));
			}
		}
	}
}
