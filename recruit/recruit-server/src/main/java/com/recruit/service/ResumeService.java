package com.recruit.service;

import com.recruit.dto.ResumeDTO;
import com.recruit.entity.Resume;

import java.util.List;

/**
 * 简历服务。
 */
public interface ResumeService {

    List<Resume> listCurrentUserResumes();

    Long create(ResumeDTO resumeDTO);

    void update(ResumeDTO resumeDTO);

    void delete(Long id);

    void setDefault(Long id);
}
