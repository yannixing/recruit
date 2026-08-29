package com.recruit.service.impl;

import com.recruit.context.BaseContext;
import com.recruit.entity.Company;
import com.recruit.exception.BaseException;
import com.recruit.mapper.CompanyMapper;
import com.recruit.service.CompanyService;
import com.recruit.service.HotJobCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 企业服务实现。
 */
@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private HotJobCacheService hotJobCacheService;

    @Override
    public Company getCurrentCompany() {
        Company company = companyMapper.getByHrId(BaseContext.getCurrentUserId());
        if (company == null) {
            throw new BaseException("当前 HR 没有企业资料");
        }
        return company;
    }

    @Override
    @Transactional
    public void update(Company company) {
        Company current = getCurrentCompany();
        company.setId(current.getId());
        company.setHrId(current.getHrId());
        company.setUpdateTime(LocalDateTime.now());
        // 热门岗位列表包含企业信息，企业资料更新前先清理相关缓存。
        hotJobCacheService.clear();
        companyMapper.update(company);
    }
}
