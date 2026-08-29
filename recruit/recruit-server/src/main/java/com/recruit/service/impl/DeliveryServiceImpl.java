package com.recruit.service.impl;

import com.recruit.constant.DeliveryStatusConstant;
import com.recruit.constant.JobStatusConstant;
import com.recruit.context.BaseContext;
import com.recruit.dto.DeliverySubmitDTO;
import com.recruit.dto.StatusUpdateDTO;
import com.recruit.entity.Delivery;
import com.recruit.entity.Job;
import com.recruit.entity.Notification;
import com.recruit.entity.Resume;
import com.recruit.entity.User;
import com.recruit.exception.BaseException;
import com.recruit.mapper.DeliveryMapper;
import com.recruit.mapper.JobMapper;
import com.recruit.mapper.NotificationMapper;
import com.recruit.mapper.ResumeMapper;
import com.recruit.mapper.UserMapper;
import com.recruit.result.PageResult;
import com.recruit.service.DeliveryService;
import com.recruit.service.HotJobCacheService;
import com.recruit.vo.DeliveryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投递业务实现。
 */
@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HotJobCacheService hotJobCacheService;

    @Override
    @Transactional
    public void submit(DeliverySubmitDTO deliverySubmitDTO) {
        Long userId = BaseContext.getCurrentUserId();
        Job job = jobMapper.getById(deliverySubmitDTO.getJobId());
        if (job == null || job.getStatus() == null || job.getStatus() != JobStatusConstant.APPROVED) {
            throw new BaseException("职位不存在或暂不可投递");
        }

        Resume resume = resumeMapper.getByIdAndUserId(deliverySubmitDTO.getResumeId(), userId);
        if (resume == null || resume.getStatus() == null || resume.getStatus() != 1) {
            throw new BaseException("简历不存在或已失效");
        }
        if (deliveryMapper.countByJobAndUser(deliverySubmitDTO.getJobId(), userId) > 0) {
            throw new BaseException("该职位已投递，请勿重复投递");
        }

        LocalDateTime now = LocalDateTime.now();
        Delivery delivery = new Delivery();
        delivery.setJobId(deliverySubmitDTO.getJobId());
        delivery.setUserId(userId);
        delivery.setResumeId(deliverySubmitDTO.getResumeId());
        delivery.setStatus(DeliveryStatusConstant.TO_COMMUNICATE);
        delivery.setDeliveryTime(now);
        delivery.setCreateTime(now);
        delivery.setUpdateTime(now);
        // 投递量参与热门岗位排序，先清理缓存，再写入投递数据。
        hotJobCacheService.clear();
        deliveryMapper.insert(delivery);
        jobMapper.refreshDeliveryCount(deliverySubmitDTO.getJobId());

        User hr = job.getHrId() == null ? null : userMapper.getById(job.getHrId());
        if (hr != null) {
            notify(hr.getId(), 1, "收到新的投递",
                    "职位“" + job.getTitle() + "”收到一份新的简历投递",
                    delivery.getId(), now);
        }
    }

    @Override
    public PageResult listForCandidate(Integer status) {
        List<DeliveryVO> list = deliveryMapper.listByUserId(BaseContext.getCurrentUserId(), status);
        return new PageResult(list.size(), list);
    }

    @Override
    public PageResult listForHr(Long jobId, Integer status) {
        List<DeliveryVO> list = deliveryMapper.listByHrId(BaseContext.getCurrentUserId(), jobId, status);
        return new PageResult(list.size(), list);
    }

    @Override
    public DeliveryVO getForCandidate(Long id) {
        Delivery delivery = deliveryMapper.getById(id);
        if (delivery == null || !BaseContext.getCurrentUserId().equals(delivery.getUserId())) {
            throw new BaseException("无权查看该投递记录");
        }
        return deliveryMapper.getVoById(id);
    }

    @Override
    public DeliveryVO getForHr(Long id) {
        Delivery delivery = deliveryMapper.getById(id);
        if (delivery == null) {
            throw new BaseException("投递记录不存在");
        }
        Job job = jobMapper.getById(delivery.getJobId());
        if (job == null || !BaseContext.getCurrentUserId().equals(job.getHrId())) {
            throw new BaseException("无权查看该投递记录");
        }
        User candidate = userMapper.getById(delivery.getUserId());
        if (candidate == null || candidate.getStatus() == null || candidate.getStatus() != 1) {
            throw new BaseException("该求职者当前不可参与招聘流程");
        }
        return deliveryMapper.getVoById(id);
    }

    @Override
    @Transactional
    public void updateStatusForHr(Long id, StatusUpdateDTO statusUpdateDTO) {
        Delivery delivery = deliveryMapper.getById(id);
        if (delivery == null) {
            throw new BaseException("投递记录不存在");
        }

        Job job = jobMapper.getById(delivery.getJobId());
        if (job == null || !BaseContext.getCurrentUserId().equals(job.getHrId())) {
            throw new BaseException("无权操作该投递记录");
        }

        Integer targetStatus = statusUpdateDTO == null ? null : statusUpdateDTO.getStatus();
        validateTransition(delivery.getStatus(), targetStatus);
        if (targetStatus == DeliveryStatusConstant.INTERVIEW) {
            throw new BaseException("进入邀约面试状态前，请先创建面试邀约");
        }
        if (targetStatus == DeliveryStatusConstant.INTERVIEW_PASS) {
            throw new BaseException("请在面试邀约中完成面试后再推进投递状态");
        }

        LocalDateTime now = LocalDateTime.now();
        Delivery update = new Delivery();
        update.setId(id);
        update.setStatus(targetStatus);
        update.setStatusRemark(statusUpdateDTO.getRemark());
        update.setUpdateTime(now);
        if (targetStatus == DeliveryStatusConstant.VIEWED) {
            update.setViewTime(now);
        } else if (targetStatus == DeliveryStatusConstant.OFFER) {
            update.setOfferTime(now);
        }
        deliveryMapper.updateStatus(update);

        notify(delivery.getUserId(), 1, "投递状态更新",
                "你投递的职位“" + job.getTitle() + "”状态已更新为：" + deliveryStatusName(targetStatus),
                id, now);
    }

    private void validateTransition(Integer current, Integer target) {
        if (target == null) {
            throw new BaseException("状态不能为空");
        }
        if (current == null || current >= DeliveryStatusConstant.UNFIT) {
            throw new BaseException("当前投递状态不能继续流转");
        }
        if (target == DeliveryStatusConstant.UNFIT || target == DeliveryStatusConstant.REJECTED) {
            return;
        }
        if (target == DeliveryStatusConstant.INTERVIEW) {
            return;
        }
        if (target != current + 1) {
            throw new BaseException("投递状态不能跨级流转");
        }
    }

    private void notify(Long userId, Integer type, String title, String content,
                        Long bizId, LocalDateTime now) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBizId(bizId);
        notification.setIsRead(0);
        notification.setCreateTime(now);
        notificationMapper.insert(notification);
    }

    private String deliveryStatusName(Integer status) {
        switch (status) {
            case DeliveryStatusConstant.TO_COMMUNICATE:
                return "待沟通";
            case DeliveryStatusConstant.VIEWED:
                return "已查看";
            case DeliveryStatusConstant.INTERVIEW:
                return "邀约面试";
            case DeliveryStatusConstant.INTERVIEW_PASS:
                return "面试通过";
            case DeliveryStatusConstant.OFFER:
                return "已发送 Offer";
            case DeliveryStatusConstant.EMPLOYED:
                return "已入职";
            case DeliveryStatusConstant.UNFIT:
                return "不合适";
            case DeliveryStatusConstant.REJECTED:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }

}
