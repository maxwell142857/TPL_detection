package com.sustech.sqllab;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sustech.sqllab.dao.ArtifactDao;
import com.sustech.sqllab.dao.VersionDao;
import com.sustech.sqllab.dao.VersionWithFingerprintDao;
import com.sustech.sqllab.po.Artifact;
import com.sustech.sqllab.po.Version;
import com.sustech.sqllab.po.VersionWithFingerprint;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;
import static org.springframework.util.ResourceUtils.getFile;

@SuppressWarnings("NewClassNamingConvention")
@SpringBootTest
public class Analyzer {

	@Resource
	private VersionWithFingerprintDao versionWithFingerprintDao;
	@Resource
	private VersionDao versionDao;
	@Resource
	private ArtifactDao artifactDao;
	private static List<String> hashes;

	@BeforeAll
	static void loadHashes() throws IOException {
		hashes = new HashSet<>(Files.readAllLines(getFile("classpath:apk.txt").toPath()))
					.stream()
					.map(cfg -> cfg.split(" "))
//					.filter(cfg -> !cfg[1].equals("1"))
					.map(cfg->cfg[0])
					.toList();
	}

	@Test
	void analyze(){
		List<VersionWithFingerprint> matchedEntries =
				versionWithFingerprintDao.selectList(
						 new LambdaQueryWrapper<VersionWithFingerprint>()
							.in(VersionWithFingerprint::getFingerprintId, hashes));
		//versionId->weight
		Map<Integer, Integer> matchedVersionIds = matchedEntries.stream()
				.collect(toMap(VersionWithFingerprint::getVersionId,
						       VersionWithFingerprint::getCount,
						       Integer::sum));
		List<Version> matchedVersions = versionDao.selectBatchIds(matchedVersionIds.keySet());
		Map<Artifact, List<Version>> groupedVersions = matchedVersions.stream().collect(groupingBy(version->artifactDao.selectById(version.getArtifactId())));
		groupedVersions.values().forEach(vers -> {
			Comparator<Version> comparator = comparingInt(ver->matchedVersionIds.get(ver.getId()));
			vers.sort(comparator.reversed());
		});

		for (Artifact artifact : groupedVersions.keySet()) {
			System.out.println("\n["+artifact.getName()+"]");
			for (Version version : groupedVersions.get(artifact)) {
				System.out.println(version.getName());
			}
		}
	}

	@Test
	@SuppressWarnings("DataFlowIssue")
	void matchWithoutDb() throws IOException{
		for (File group : getFile("classpath:root").listFiles()) {
			for (File artifact : group.listFiles()) {
				version:for (File version : artifact.listFiles()) {
					for (String cfg : Files.readAllLines(version.toPath())) {
						if(hashes.contains(cfg.split(" ")[0])){
							System.out.printf("[%s][%s][%s] matched\n",group.getName(),artifact.getName(),version.getName());
							continue version;
						}
					}
					System.out.printf("[%s][%s][%s] not matched\n",group.getName(),artifact.getName(),version.getName());
				}
			}
		}
	}

	@Test
	void testOneVersion() throws IOException{
		Set<String> versionHashes =  new HashSet<>(Files.readAllLines(getFile("classpath:root/androidx.activity/activity-compose/1.6.0.txt").toPath()))
										.stream()
										.map(cfg -> cfg.split(" "))
										.map(cfg->cfg[0])
										.collect(toSet());
		int count=0;
		for (String hash : hashes) {
			if(versionHashes.contains(hash)){
				count++;
			}
		}
		System.out.println(count);
	}

	@Test
	@SuppressWarnings("DataFlowIssue")
	@SneakyThrows(IOException.class)
	void compareMethodName(){
//		Set<String> tplMethods = Arrays.stream(getFile("classpath:root").listFiles())
//										.flatMap(group -> Arrays.stream(group.listFiles()))
//										.flatMap(artifact -> Arrays.stream(artifact.listFiles()))
//										.flatMap(version -> {
//											try {
//												return Files.readAllLines(version.toPath()).stream();
//											} catch (IOException ignored) {
//												return null;
//											}
//										})
//										.collect(toSet());
		Set<String> methods =  new HashSet<>(Files.readAllLines(getFile("classpath:methodName.txt").toPath()));
//		methods.retainAll(tplMethods);
		for (File group : getFile("classpath:root").listFiles()) {
			for (File artifact : group.listFiles()) {
				for (File version : artifact.listFiles()) {
					for (String method : Files.readAllLines(version.toPath())) {
						if(methods.contains(method)){
							System.out.printf("[%s][%s][%s]\n",group.getName(),artifact.getName(),version.getName());
							break;
						}
					}
				}
			}
		}
	}

	@Test
	@SneakyThrows(IOException.class)
	void lineNum(){
		System.out.println(Files.readAllLines(getFile("classpath:methodName.txt").toPath()).size());
	}
}
