package com.sustech.sqllab;

import com.sustech.sqllab.dao.ArtifactDao;
import com.sustech.sqllab.dao.FingerprintDao;
import com.sustech.sqllab.dao.GroupDao;
import com.sustech.sqllab.dao.VersionDao;
import com.sustech.sqllab.po.Artifact;
import com.sustech.sqllab.po.Group;
import com.sustech.sqllab.po.Version;
import lombok.SneakyThrows;
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

@SpringBootApplication
@MapperScan("com.sustech.sqllab.dao")
public class DbImporterApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(DbImporterApplication.class, args);
	}

	@Value("${root-path}")
	private String rootPath;
	@Resource
	private VersionDao versionDao;
	@Resource
	private ArtifactDao artifactDao;
	@Resource
	private GroupDao groupDao;
	@Resource
	private FingerprintDao fingerprintDao;

	@SuppressWarnings("ConstantConditions")
	@SneakyThrows(IOException.class)
	@Override
	public void run(ApplicationArguments args){
		for (File groupDir : new File(rootPath).listFiles()) {
			Group group = new Group(null, groupDir.getName());
			groupDao.insert(group);
			for (File artifactDir : groupDir.listFiles()) {
				Artifact artifact = new Artifact()
									   .setGroupId(group.getId())
									   .setName(artifactDir.getName());
				artifactDao.insert(artifact);
				for (File versionFile : artifactDir.listFiles()) {
					String versionName = versionFile.getName();
					Version version =new Version()
										.setArtifactId(artifact.getId())
										.setName(versionName);
					versionDao.insert(version);
					Files.readAllLines(versionFile.toPath());
				}
			}
		}
	}
}
