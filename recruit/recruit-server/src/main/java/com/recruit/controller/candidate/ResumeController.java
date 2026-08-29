package com.recruit.controller.candidate;

import com.recruit.dto.ResumeDTO;
import com.recruit.entity.Resume;
import com.recruit.result.Result;
import com.recruit.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 求职者简历接口。
 */
@RestController
@RequestMapping("/candidate/resumes")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @GetMapping
    public Result<List<Resume>> list() {
        return Result.success(resumeService.listCurrentUserResumes());
    }

    @PostMapping
    public Result<Long> create(@RequestBody ResumeDTO resumeDTO) {
        return Result.success(resumeService.create(resumeDTO));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ResumeDTO resumeDTO) {
        resumeDTO.setId(id);
        resumeService.update(resumeDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resumeService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        resumeService.setDefault(id);
        return Result.success();
    }
}
