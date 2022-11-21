package com.sustech.sqllab;

import com.sustech.sqllab.dao.FingerprintDao;
import com.sustech.sqllab.dao.VersionDao;
import com.sustech.sqllab.po.Fingerprint;
import com.sustech.sqllab.po.Version;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.springframework.util.ResourceUtils.getFile;

@SuppressWarnings("NewClassNamingConvention")
@SpringBootTest
public class Analyzer {

	@Resource
	private FingerprintDao fingerprintDao;
	@Resource
	private VersionDao versionDao;
	@Test
	void analyze() throws IOException {
		HashSet<String> hashes = new HashSet<>(Files.readAllLines(getFile("classpath:result.txt").toPath()));
		System.out.println(versionDao.selectBatchIds(
						fingerprintDao.selectBatchIds(hashes)
								.stream()
								.map(Fingerprint::getVersionId)
								.collect(Collectors.toSet()))
				.stream().map(Version::getName).collect(Collectors.toList()));
	}
}
