package com.recruit.service;

import com.recruit.vo.JobVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 热门岗位缓存服务。
 *
 * <p>热门岗位采用旁路缓存模式：读取时先查 Redis，未命中时查询 MySQL；
 * 岗位、企业或投递数据发生变化时，先删除缓存，再执行数据库写操作。</p>
 */
@Service
@Slf4j
public class HotJobCacheService {

    private static final String HOT_JOB_CACHE_PREFIX = "job:hot:";
    private static final long CACHE_MINUTES = 30L;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 读取全站热门岗位缓存。
     *
     * @return 缓存命中时返回岗位列表，Redis 不可用或未命中时返回 null
     */
    @SuppressWarnings("unchecked")
    public List<JobVO> get() {
        try {
            Object cachedValue = redisTemplate.opsForValue().get(buildKey());
            if (cachedValue instanceof List) {
                return (List<JobVO>) cachedValue;
            }
        } catch (Exception ex) {
            // Redis 读取失败时返回 null，由调用方降级查询 MySQL。
            log.warn("读取热门岗位缓存失败，key={}", buildKey(), ex);
        }
        return null;
    }

    /**
     * 写入热门岗位缓存，默认缓存 30 分钟。
     */
    public void set(List<JobVO> jobs) {
        try {
            redisTemplate.opsForValue().set(buildKey(), jobs, CACHE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ex) {
            // Redis 写入失败时不影响热门岗位主流程。
            log.warn("写入热门岗位缓存失败，key={}", buildKey(), ex);
        }
    }

    /**
     * 清理所有城市的热门岗位缓存。
     */
    public void clear() {
        try {
            Set<String> keys = redisTemplate.keys(HOT_JOB_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ex) {
            // Redis 清理失败时不影响数据库主流程。
            log.warn("清理热门岗位缓存失败，pattern={}", HOT_JOB_CACHE_PREFIX + "*", ex);
        }
    }

    /**
     * 构造全站热门岗位缓存 key。
     */
    private String buildKey() {
        return HOT_JOB_CACHE_PREFIX + "all";
    }
}
