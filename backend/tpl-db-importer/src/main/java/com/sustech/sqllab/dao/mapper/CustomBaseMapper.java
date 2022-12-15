package com.sustech.sqllab.dao.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public interface CustomBaseMapper<T> extends MPJBaseMapper<T> {

	int insertBatchSomeColumn(@Param("list") List<T> entities);
}
