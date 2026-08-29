package com.recruit.mapper;

import com.recruit.entity.Resume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 简历数据访问接口。
 */
@Mapper
public interface ResumeMapper {

    List<Resume> listByUserId(@Param("userId") Long userId);

    Resume getById(@Param("id") Long id);

    Resume getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void insert(Resume resume);

    void update(Resume resume);

    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void clearDefaultByUserId(@Param("userId") Long userId);

    void setDefault(@Param("id") Long id, @Param("userId") Long userId);
}
