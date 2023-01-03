package com.sustech.sqllab;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sustech.sqllab.dao.ArtifactDao;
import com.sustech.sqllab.dao.VersionDao;
import com.sustech.sqllab.dao.VersionWithFingerprintDao;
import com.sustech.sqllab.po.Artifact;
import com.sustech.sqllab.po.Version;
import com.sustech.sqllab.po.VersionWithFingerprint;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

@SuppressWarnings("NewClassNamingConvention")
@SpringBootTest
public class Analyzer {

	@Resource
	private VersionWithFingerprintDao versionWithFingerprintDao;
	@Resource
	private VersionDao versionDao;
	@Resource
	private ArtifactDao artifactDao;

	@Test
	void analyzeOneApk(){
		System.out.print(analyzeHashes("src/main/resources/result.txt"));
	}

	@SuppressWarnings("DataFlowIssue")
	@Test
	void analyzeApks() throws IOException {
		for (File apk : new File("src/main/resources/apk").listFiles()) {
			String apkPath = apk.getAbsolutePath();
			@Cleanup FileWriter writer=new FileWriter(apkPath+"/report.txt");
			writer.write(analyzeHashes(apkPath+"/result.txt"));
			System.out.println(apk.getName()+" Done");
		}
	}

	/**
	 * @return 匹配结果
	 */
	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	@SneakyThrows(IOException.class)
	private String analyzeHashes(String apkHashesPath){
		List<String> apkHashes =new HashSet<>(Files.readAllLines(Path.of(apkHashesPath)))
												.stream()
												.map(cfg -> cfg.split(" "))
//												.filter(cfg -> !cfg[1].equals("1"))
												.map(cfg->cfg[0])
												.toList();
		List<VersionWithFingerprint> matchedEntries =
				versionWithFingerprintDao.selectList(
						new LambdaQueryWrapper<VersionWithFingerprint>()
								.in(VersionWithFingerprint::getFingerprintId, apkHashes));
		//versionId->weight
		Map<Integer, Integer> matchedVersionIds = matchedEntries.stream()
				.collect(toMap(VersionWithFingerprint::getVersionId,
						VersionWithFingerprint::getCount,
						Integer::sum));
		List<Version> matchedVersions = versionDao.selectBatchIds(matchedVersionIds.keySet());
		Set<Integer> matchedArtifactIds= matchedVersions.stream()
														.map(Version::getArtifactId)
														.collect(toSet());
		List<Artifact> matchedArtifacts = artifactDao.selectBatchIds(matchedArtifactIds);
		//Version->Artifact
		Map<Version,Artifact> artifactMap=new HashMap<>();
		for (Version version : matchedVersions) {
			for (Artifact artifact : matchedArtifacts) {
				if(artifact.getId().equals(version.getArtifactId())){
					artifactMap.put(version,artifact);
					break;
				}
			}
		}
		Map<Artifact, List<Version>> groupedVersions = matchedVersions.stream().collect(groupingBy(artifactMap::get));
		groupedVersions.values().forEach(vers -> {
			Comparator<Version> comparator = comparingInt(ver->matchedVersionIds.get(ver.getId()));
			vers.sort(comparator.reversed());
		});

		StringBuilder res = new StringBuilder();
		for (Artifact artifact : groupedVersions.keySet()) {
			res.append("["+artifact.getName()+"]\n");
			for (Version version : groupedVersions.get(artifact)) {
				res.append(version.getName()+"\n");
			}
			res.append('\n');
		}
		return res.toString();
	}
}
