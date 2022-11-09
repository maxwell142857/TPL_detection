package com.sustech.sqllab.importer.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionWithPermission {
	private Integer versionId;
	private Integer permissionId;
}
