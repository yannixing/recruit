package com.recruit.service;

import com.recruit.entity.Company;

/**
 * 企业服务。
 */
public interface CompanyService {

    Company getCurrentCompany();

    void update(Company company);
}
