package com.sustech.sqllab.dao.mapper;

import com.github.yulichang.base.MPJBaseMapper;

import java.util.Collection;

public interface CustomBaseMapper<T> extends MPJBaseMapper<T> {

	int insertBatchSomeColumn(Collection<T> entities);
}
