package com.recruit.service.impl;

import com.recruit.context.BaseContext;
import com.recruit.dto.ResumeDTO;
import com.recruit.entity.Resume;
import com.recruit.exception.BaseException;
import com.recruit.mapper.ResumeMapper;
import com.recruit.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 简历服务实现。
 */
@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public List<Resume> listCurrentUserResumes() {
        return resumeMapper.listByUserId(BaseContext.getCurrentUserId());
    }

    @Override
    @Transactional
    public Long create(ResumeDTO resumeDTO) {
        validate(resumeDTO);
        Long userId = BaseContext.getCurrentUserId();
        Resume resume = new Resume();
        BeanUtils.copyProperties(resumeDTO, resume);
        resume.setUserId(userId);
        resume.setStatus(1);
        resume.setIsDefault(resumeDTO.getIsDefault() != null && resumeDTO.getIsDefault() == 1 ? 1 : 0);
        if (resume.getIsDefault() == 1) {
            resumeMapper.clearDefaultByUserId(userId);
        } else if (resumeMapper.listByUserId(userId).isEmpty()) {
            resume.setIsDefault(1);
        }
        resume.setCreateTime(java.time.LocalDateTime.now());
        resume.setUpdateTime(resume.getCreateTime());
        resumeMapper.insert(resume);
        return resume.getId();
    }

    @Override
    @Transactional
    public void update(ResumeDTO resumeDTO) {
        validate(resumeDTO);
        if (resumeDTO.getId() == null) {
            throw new BaseException("简历编号不能为空");
        }
        Long userId = BaseContext.getCurrentUserId();
        Resume existing = resumeMapper.getByIdAndUserId(resumeDTO.getId(), userId);
        if (existing == null || existing.getStatus() == null || existing.getStatus() != 1) {
            throw new BaseException("简历不存在或已失效");
        }
        Resume resume = new Resume();
        BeanUtils.copyProperties(resumeDTO, resume);
        resume.setId(existing.getId());
        resume.setUserId(userId);
        if (resumeDTO.getIsDefault() != null && resumeDTO.getIsDefault() == 1) {
            resumeMapper.clearDefaultByUserId(userId);
            resume.setIsDefault(1);
        } else {
            resume.setIsDefault(existing.getIsDefault());
        }
        resume.setUpdateTime(java.time.LocalDateTime.now());
        resumeMapper.update(resume);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = BaseContext.getCurrentUserId();
        Resume existing = resumeMapper.getByIdAndUserId(id, userId);
        if (existing == null || existing.getStatus() == null || existing.getStatus() != 1) {
            throw new BaseException("简历不存在或已失效");
        }
        resumeMapper.deleteByIdAndUserId(id, userId);
        if (existing.getIsDefault() != null && existing.getIsDefault() == 1) {
            List<Resume> resumes = resumeMapper.listByUserId(userId);
            if (!resumes.isEmpty()) {
                resumeMapper.setDefault(resumes.get(0).getId(), userId);
            }
        }
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        Long userId = BaseContext.getCurrentUserId();
        Resume existing = resumeMapper.getByIdAndUserId(id, userId);
        if (existing == null || existing.getStatus() == null || existing.getStatus() != 1) {
            throw new BaseException("简历不存在或已失效");
        }
        resumeMapper.clearDefaultByUserId(userId);
        resumeMapper.setDefault(id, userId);
    }

    private void validate(ResumeDTO resumeDTO) {
        if (resumeDTO == null || resumeDTO.getName() == null || resumeDTO.getName().trim().isEmpty()
                || resumeDTO.getPhone() == null || resumeDTO.getPhone().trim().isEmpty()) {
            throw new BaseException("姓名和手机号不能为空");
        }
    }
}
