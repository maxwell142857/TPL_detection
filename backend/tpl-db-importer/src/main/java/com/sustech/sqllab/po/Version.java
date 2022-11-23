package com.sustech.sqllab.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
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
