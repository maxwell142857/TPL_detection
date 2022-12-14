package com.sustech.sqllab.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
	@TableId(type= IdType.AUTO)
	private Integer id;
	private String name;
	private Integer versionAdded;
	private String description;
	private Integer versionDeprecated;
}
