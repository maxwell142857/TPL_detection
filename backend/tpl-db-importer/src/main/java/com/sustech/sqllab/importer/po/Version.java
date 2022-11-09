package com.sustech.sqllab.importer.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Version {
	@TableId(type= IdType.AUTO)
	private Integer id;
	private Integer artifactId;
	private Integer minSdkVersion;
	private Integer targetSdkVersion;
	@TableField("`usage`")
	private Integer usage;
	private Date date;
	private String name;
}
