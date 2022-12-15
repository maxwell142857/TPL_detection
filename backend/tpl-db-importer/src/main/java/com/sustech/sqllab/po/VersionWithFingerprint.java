package com.sustech.sqllab.po;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VersionWithFingerprint {
	private Integer versionId;
	private String fingerprintId;
	private Integer count;
}
