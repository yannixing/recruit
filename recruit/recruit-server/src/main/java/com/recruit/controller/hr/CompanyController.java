package com.recruit.controller.hr;

import com.recruit.entity.Company;
import com.recruit.result.Result;
import com.recruit.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HR 企业资料接口。
 */
@RestController
@RequestMapping("/hr/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public Result<Company> get() {
        return Result.success(companyService.getCurrentCompany());
    }

    @PutMapping
    public Result<Void> update(@RequestBody Company company) {
        companyService.update(company);
        return Result.success();
    }
}
