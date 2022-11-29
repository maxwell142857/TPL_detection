package com.sustech.sqllab;

import com.sustech.sqllab.dao.*;
import com.sustech.sqllab.dao.mapper.CustomBaseMapper;
import com.sustech.sqllab.po.*;
import lombok.SneakyThrows;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

@SpringBootApplication
//如果不指定markerInterface，那么CustomBaseMapper也会被扫描成DAO
@MapperScan(value = "com.sustech.sqllab.dao", markerInterface = CustomBaseMapper.class)
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
	@Resource
	private VersionWithFingerprintDao versionWithFingerprintDao;

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
										.setName(versionName.replace(".txt",""));
					versionDao.insert(version);
					for (String cfg : new HashSet<>(Files.readAllLines(versionFile.toPath()))) {
						String[] split = cfg.split(" ");
						String hash = split[0];
						int node = Integer.parseInt(split[1]);
						try {
							fingerprintDao.insert(new Fingerprint()
													 .setId(hash)
													 .setNodeCount(node));
						}catch(DuplicateKeyException ignored){}
						versionWithFingerprintDao.insert(new VersionWithFingerprint()
															.setVersionId(version.getId())
															.setFingerprintId(hash));
					}
					System.out.println(versionName);
				}
			}
		}
	}
}
