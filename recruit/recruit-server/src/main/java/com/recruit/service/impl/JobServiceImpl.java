package com.recruit.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.recruit.constant.JobStatusConstant;
import com.recruit.context.BaseContext;
import com.recruit.dto.JobDTO;
import com.recruit.dto.JobPageQueryDTO;
import com.recruit.dto.StatusUpdateDTO;
import com.recruit.entity.Company;
import com.recruit.entity.Job;
import com.recruit.exception.BaseException;
import com.recruit.exception.DeletionNotAllowedException;
import com.recruit.mapper.CompanyMapper;
import com.recruit.mapper.JobMapper;
import com.recruit.result.PageResult;
import com.recruit.service.HotJobCacheService;
import com.recruit.service.JobService;
import com.recruit.vo.JobVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 职位业务实现。
 */
@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private HotJobCacheService hotJobCacheService;

    @Override
    @Transactional
    public void create(JobDTO jobDTO) {
        Long hrId = BaseContext.getCurrentUserId();
        Company company = resolveCompany(hrId, jobDTO.getCompanyId());
        Job job = new Job();
        BeanUtils.copyProperties(jobDTO, job);
        job.setCompanyId(company.getId());
        job.setHrId(hrId);
        if (job.getStatus() == null) {
            job.setStatus(JobStatusConstant.PENDING);
        }
        // 先清理缓存，再写入职位，避免旧缓存继续返回过期数据。
        hotJobCacheService.clear();
        jobMapper.insert(job);
    }

    @Override
    @Transactional
    public void update(JobDTO jobDTO) {
        Job existingJob = requireOwnedJob(jobDTO.getId());
        Long hrId = BaseContext.getCurrentUserId();
        Long companyId = jobDTO.getCompanyId() == null ? existingJob.getCompanyId() : jobDTO.getCompanyId();
        Company company = resolveCompany(hrId, companyId);
        Job job = new Job();
        BeanUtils.copyProperties(jobDTO, job);
        job.setHrId(existingJob.getHrId());
        job.setCompanyId(company.getId());
        if (job.getStatus() == null) {
            job.setStatus(existingJob.getStatus());
        }
        // 职位信息可能出现在热门岗位列表中，更新前先清理缓存。
        hotJobCacheService.clear();
        jobMapper.update(job);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        requireOwnedJob(id);
        validateStatus(status);
        Job job = new Job();
        job.setId(id);
        job.setStatus(status);
        // 上下架会改变候选人可见的热门岗位列表。
        hotJobCacheService.clear();
        jobMapper.update(job);
    }

    @Override
    @Transactional
    public void review(Long id, StatusUpdateDTO statusUpdateDTO) {
        if (statusUpdateDTO == null || statusUpdateDTO.getStatus() == null) {
            throw new BaseException("审核状态不能为空");
        }
        if (statusUpdateDTO.getStatus() != JobStatusConstant.APPROVED
                && statusUpdateDTO.getStatus() != JobStatusConstant.REJECTED) {
            throw new BaseException("审核状态只能是通过或驳回");
        }
        // 审核结果会改变候选人端可见数据，先清理缓存再更新数据库。
        hotJobCacheService.clear();
        jobMapper.review(id, statusUpdateDTO.getStatus(), statusUpdateDTO.getRemark());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BaseException("请选择需要删除的职位");
        }
        for (Long id : ids) {
            requireOwnedJob(id);
            if (jobMapper.countDeliveriesByJobId(id) > 0) {
                throw new DeletionNotAllowedException("职位已有投递记录，不能删除");
            }
        }
        // 删除职位前先清理热门岗位缓存。
        hotJobCacheService.clear();
        jobMapper.deleteByIds(ids);
    }

    @Override
    public PageResult pageForHr(JobPageQueryDTO query) {
        query.setHrId(BaseContext.getCurrentUserId());
        return pageQuery(query);
    }

    @Override
    public PageResult pageForAdmin(JobPageQueryDTO query) {
        return pageQuery(query);
    }

    @Override
    public PageResult pageForAdminPendingReview(JobPageQueryDTO query) {
        query.setHrId(null);
        query.setStatus(JobStatusConstant.PENDING);
        return pageQuery(query);
    }

    @Override
    public PageResult pageForCandidate(JobPageQueryDTO query) {
        query.setHrId(null);
        query.setStatus(JobStatusConstant.APPROVED);
        return pageQuery(query);
    }

    @Override
    public JobVO getForHr(Long id) {
        requireOwnedJob(id);
        return jobMapper.getVoById(id);
    }

    @Override
    public JobVO getForAdmin(Long id) {
        JobVO jobVO = jobMapper.getVoById(id);
        if (jobVO == null) {
            throw new BaseException("职位不存在");
        }
        return jobVO;
    }

    @Override
    public JobVO getForCandidate(Long id) {
        JobVO jobVO = jobMapper.getVoById(id);
        if (jobVO == null || jobVO.getStatus() == null || jobVO.getStatus() != JobStatusConstant.APPROVED) {
            throw new BaseException("职位不存在或已下架");
        }
        // 浏览量参与热门排序，更新前先清理热门岗位缓存。
        hotJobCacheService.clear();
        jobMapper.incrementViewCount(id);
        JobVO updated = jobMapper.getVoById(id);
        return updated == null ? jobVO : updated;
    }

    @Override
    public List<JobVO> listHot() {
        List<JobVO> cachedJobs = hotJobCacheService.get();
        if (cachedJobs != null) {
            return cachedJobs;
        }

        List<JobVO> jobs = jobMapper.listHot();
        hotJobCacheService.set(jobs);
        return jobs;
    }

    private PageResult pageQuery(JobPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<JobVO> page = jobMapper.pageQuery(query);
        return new PageResult(page.getTotal(), page.getResult());
    }

    private Job requireOwnedJob(Long id) {
        if (id == null) {
            throw new BaseException("职位编号不能为空");
        }
        Job job = jobMapper.getById(id);
        if (job == null) {
            throw new BaseException("职位不存在");
        }
        Long currentUserId = BaseContext.getCurrentUserId();
        if (!currentUserId.equals(job.getHrId())) {
            throw new BaseException("无权操作其他 HR 发布的职位");
        }
        return job;
    }

    private Company resolveCompany(Long hrId, Long companyId) {
        Company company = null;
        if (companyId != null) {
            company = companyMapper.getById(companyId);
            if (company != null && !hrId.equals(company.getHrId())) {
                throw new BaseException("无权使用其他 HR 的企业信息");
            }
        }
        if (company == null) {
            company = companyMapper.getByHrId(hrId);
        }
        if (company == null) {
            throw new BaseException("当前 HR 没有绑定企业信息");
        }
        return company;
    }

    private void validateStatus(Integer status) {
        if (status == null || status < 0 || status > 3) {
            throw new BaseException("职位状态不合法");
        }
    }

}
