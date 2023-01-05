package com.sustech.sqllab;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sustech.sqllab.dao.ArtifactDao;
import com.sustech.sqllab.dao.GroupDao;
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

import static com.sustech.sqllab.util.FileDownloader.downloadFile;
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
	@Resource
	private GroupDao groupDao;

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

	@SuppressWarnings("DataFlowIssue")
	@Test
	void calculateFScore() throws IOException {
		File[] apks = new File("src/main/resources/apk").listFiles();
//      //Auto parse TPL from build.gradle(.kts) URL
//		for (File apk : apks) {
//			//1.Download build.gradle
//			String url =Arrays.stream(apk.listFiles())
//							  .map(File::getName)
//							  .filter(name -> name.equals("link.txt"))
//							  .toList()
//							  .get(0);
//			String fileName = url.substring(url.lastIndexOf("/") + 1);
//			String gradleFile = apk.getAbsolutePath() + "/" + fileName;
//			downloadFile(url, gradleFile);
//			System.out.println("Download "+fileName+" Done");
//			//2.Parse build.gradle
//			//https://docs.gradle.org/current/userguide/declaring_dependencies.html
//			Files.readAllLines(Path.of(gradleFile))
//					.stream()
//					.filter(line->line.contains("mplementation") &&
//								!line.contains(""))
//		}
		for (File apk : apks) {
			String apkPath = apk.getAbsolutePath();
			List<String> trueArtifacts=Files.readAllLines(Path.of(apkPath + "/tpl.txt"))
											.stream()
											.map(line->line.substring(0,line.lastIndexOf(":")))
											.toList();
			List<String> reportArtifacts = Files.readAllLines(Path.of(apkPath + "/report.txt"))
												.stream()
												.filter(line -> line.startsWith("["))
												.map(line ->line.replace("[", "")
																.replace("]", ""))
												.toList();
			HashSet<String> matchedArtifacts = new HashSet<>(reportArtifacts);
			matchedArtifacts.retainAll(trueArtifacts);
			int tp = matchedArtifacts.size();
			String apkName = Arrays.stream(apk.listFiles())
								   .map(File::getName)
								   .filter(name -> name.endsWith(".apk"))
								   .toList().get(0);
			System.out.println("\n"+apkName);
			//1.Calculate Precision=TP/TP+FP
			System.out.printf("Precision: %.2f\n",(float)tp/reportArtifacts.size());
			//2.Calculate Recall=TP/TP+FN
			System.out.printf("Recall: %.2f\n",(float)tp/trueArtifacts.size());
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
			String groupName = groupDao.selectById(artifact.getGroupId()).getName();
			res.append("["+groupName+":"+artifact.getName()+"]\n");
			for (Version version : groupedVersions.get(artifact)) {
				res.append(matchedVersionIds.get(version.getId())+" "+version.getName()+"\n");
			}
			res.append('\n');
		}
		return res.toString();
	}
}
