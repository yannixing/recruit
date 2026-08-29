package com.recruit.mapper;

import com.recruit.entity.Company;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业数据访问接口。
 */
@Mapper
public interface CompanyMapper {

    Company getById(Long id);

    Company getByHrId(Long hrId);

    void insert(Company company);

    void update(Company company);
}
