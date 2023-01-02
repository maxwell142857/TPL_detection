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

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.*;
import static org.springframework.util.StringUtils.hasText;

@SpringBootApplication
//如果不指定markerInterface，那么CustomBaseMapper也会被扫描成DAO
@MapperScan(value = "com.sustech.sqllab.dao", markerInterface = CustomBaseMapper.class)
public class DbImporterApplication /*implements ApplicationRunner*/ {

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
//	@Override
	public void run(ApplicationArguments args){
		//Todo:仓库未更新最新version，甚至可能导致检测不出来artifact
		long start = currentTimeMillis();
		int totalArtifacts=(int)Arrays.stream(new File(rootPath).listFiles())
									  .flatMap(group-> Arrays.stream(group.listFiles()))
									  .count();
		int artifactCount=0;
		int cfgCount=0;
		for (File groupDir : new File(rootPath).listFiles()) {
			Group group = new Group(null, groupDir.getName());
			groupDao.insert(group);
			for (File artifactDir : groupDir.listFiles()) {
				System.out.printf("\n----------%d/%d [%s]--------\n",++artifactCount,totalArtifacts,artifactDir.getName());
				Artifact artifact = new Artifact()
									   .setGroupId(group.getId())
									   .setName(artifactDir.getName());
				artifactDao.insert(artifact);
				for (File versionFile : artifactDir.listFiles()) {
					String versionName = versionFile.getName().replace(".txt", "");
					if(hasText(Files.readString(versionFile.toPath()))){ //Some version file have no CFGs
						Version version =new Version()
											.setArtifactId(artifact.getId())
											.setName(versionName);
						versionDao.insert(version);
						//cfg->count
						Map<String, Integer> cfgs =Files.readAllLines(versionFile.toPath())
														.stream()
														.collect(toMap(cfg->cfg, cfg->1, Integer::sum));

						//1. Batch insert the none existed fingerprints
						Set<Fingerprint> insertFingerPrints =cfgs.keySet()
															.stream()
															.map(cfg ->{String[] split = cfg.split(" ");
																		return new Fingerprint()
																				  .setId(split[0])
																				  .setNodeCount(1);
	//																			  .setNodeCount(parseInt(split[1]));
															}).collect(toSet());
						List<String> hashes = insertFingerPrints.stream().map(Fingerprint::getId).toList();
						Set<Fingerprint> existFingerPrints = new HashSet<>(fingerprintDao.selectBatchIds(hashes));
						int originCfgs = insertFingerPrints.size();
						insertFingerPrints.removeAll(existFingerPrints);
						if(!insertFingerPrints.isEmpty()) {
							fingerprintDao.insertBatchSomeColumn(new ArrayList<>(insertFingerPrints));
						}

						//2. Batch insert the VersionWithFingerprint
						List<VersionWithFingerprint> versionWithFingerprints =
								cfgs.entrySet()
									.stream()
									.map(e->{
										String[] split = e.getKey().split(" ");
										return new VersionWithFingerprint()
												  .setVersionId(version.getId())
												  .setFingerprintId(split[0])
												  .setCount(e.getValue());
									}).toList();
						versionWithFingerprintDao.insertBatchSomeColumn(versionWithFingerprints);
						cfgCount+=insertFingerPrints.size();
						System.out.printf("[%s] has %d/%d CFGs\n",versionName,insertFingerPrints.size(),originCfgs);
					}else{System.out.printf("[%s] is empty!\n",versionName);}
				}
			}
		}
		Date time = new Date(currentTimeMillis()-start+16*60*60*1000);
		System.out.printf("---------------Total Cost %s with %d CFGs---------------\n",new SimpleDateFormat("HH:mm:ss").format(time),cfgCount);
	}
}
