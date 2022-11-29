package com.sustech.sqllab.po;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Fingerprint {
	private String id;
	private Integer nodeCount;
}
