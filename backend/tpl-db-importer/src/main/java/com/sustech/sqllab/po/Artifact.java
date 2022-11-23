package com.sustech.sqllab.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Artifact {
	@TableId(type= IdType.AUTO)
	private Integer id;
	private String name;
	private Integer usage;
	private String packageName;
	private String description;
	private String license;
	private String developer;
	private String categories;
	private Integer rank;
	private Integer groupId;
}
