package com.recruit.service.impl;

import com.recruit.constant.DeliveryStatusConstant;
import com.recruit.constant.InterviewStatusConstant;
import com.recruit.context.BaseContext;
import com.recruit.dto.InterviewDTO;
import com.recruit.dto.StatusUpdateDTO;
import com.recruit.entity.Delivery;
import com.recruit.entity.Interview;
import com.recruit.entity.Job;
import com.recruit.entity.Notification;
import com.recruit.exception.BaseException;
import com.recruit.mapper.DeliveryMapper;
import com.recruit.mapper.InterviewMapper;
import com.recruit.mapper.JobMapper;
import com.recruit.mapper.NotificationMapper;
import com.recruit.mapper.UserMapper;
import com.recruit.result.PageResult;
import com.recruit.service.InterviewService;
import com.recruit.vo.InterviewVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试邀约业务实现。
 */
@Service
public class InterviewServiceImpl implements InterviewService {

    @Autowired
    private InterviewMapper interviewMapper;

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult listForCandidate() {
        List<InterviewVO> list = interviewMapper.listByUserId(BaseContext.getCurrentUserId());
        return new PageResult(list.size(), list);
    }

    @Override
    public PageResult listForHr() {
        List<InterviewVO> list = interviewMapper.listByHrId(BaseContext.getCurrentUserId());
        return new PageResult(list.size(), list);
    }

    @Override
    @Transactional
    public Long create(InterviewDTO interviewDTO) {
        validateInterview(interviewDTO);
        Delivery delivery = requireHrDelivery(interviewDTO.getDeliveryId());
        if (delivery.getStatus() == null || delivery.getStatus() != DeliveryStatusConstant.VIEWED) {
            throw new BaseException("只有已查看的投递才能创建面试邀约");
        }

        LocalDateTime now = LocalDateTime.now();
        Interview interview = new Interview();
        BeanUtils.copyProperties(interviewDTO, interview);
        interview.setHrId(BaseContext.getCurrentUserId());
        interview.setStatus(InterviewStatusConstant.PENDING);
        interview.setCreateTime(now);
        interview.setUpdateTime(now);
        interviewMapper.insert(interview);

        Delivery update = new Delivery();
        update.setId(delivery.getId());
        update.setStatus(DeliveryStatusConstant.INTERVIEW);
        update.setInterviewTime(interviewDTO.getInterviewTime());
        update.setStatusRemark("已创建面试邀约");
        update.setUpdateTime(now);
        deliveryMapper.updateStatus(update);

        Job job = jobMapper.getById(delivery.getJobId());
        notifyCandidate(delivery.getUserId(), job, interview.getId(),
                "你收到一份面试邀约，面试时间：" + interviewDTO.getInterviewTime());
        return interview.getId();
    }

    @Override
    @Transactional
    public void update(Long id, InterviewDTO interviewDTO) {
        validateInterview(interviewDTO);
        Interview existing = requireHrInterview(id);
        if (existing.getStatus() == InterviewStatusConstant.REJECTED
                || existing.getStatus() == InterviewStatusConstant.CANCELED
                || existing.getStatus() == InterviewStatusConstant.FINISHED) {
            throw new BaseException("当前面试状态不能修改");
        }

        Interview update = new Interview();
        BeanUtils.copyProperties(interviewDTO, update);
        update.setId(id);
        update.setUpdateTime(LocalDateTime.now());
        interviewMapper.update(update);

        Delivery delivery = deliveryMapper.getById(existing.getDeliveryId());
        Job job = jobMapper.getById(delivery.getJobId());
        notifyCandidate(delivery.getUserId(), job, id,
                "面试邀约已修改，新的面试时间：" + interviewDTO.getInterviewTime());
    }

    @Override
    @Transactional
    public void cancel(Long id, String remark) {
        Interview existing = requireHrInterview(id);
        if (existing.getStatus() == InterviewStatusConstant.REJECTED
                || existing.getStatus() == InterviewStatusConstant.CANCELED
                || existing.getStatus() == InterviewStatusConstant.FINISHED) {
            throw new BaseException("当前面试状态不能取消");
        }

        String feedback = remark == null || remark.trim().isEmpty()
                ? "HR 已取消面试邀约" : remark;
        interviewMapper.updateStatus(id, InterviewStatusConstant.CANCELED, feedback);

        Delivery delivery = deliveryMapper.getById(existing.getDeliveryId());
        Job job = jobMapper.getById(delivery.getJobId());
        Delivery deliveryUpdate = new Delivery();
        deliveryUpdate.setId(delivery.getId());
        deliveryUpdate.setStatusRemark(feedback);
        deliveryUpdate.setUpdateTime(LocalDateTime.now());
        deliveryMapper.updateStatus(deliveryUpdate);
        notifyCandidate(delivery.getUserId(), job, id, feedback);
    }

    @Override
    @Transactional
    public void updateStatusForHr(Long id, StatusUpdateDTO statusUpdateDTO) {
        Interview existing = requireHrInterview(id);
        Integer target = statusUpdateDTO == null ? null : statusUpdateDTO.getStatus();
        if (target == null
                || (target != InterviewStatusConstant.FINISHED
                && target != InterviewStatusConstant.REJECTED)
                || (existing.getStatus() != InterviewStatusConstant.PENDING
                && existing.getStatus() != InterviewStatusConstant.CONFIRMED)) {
            throw new BaseException("当前面试只能标记为通过或不通过");
        }

        interviewMapper.updateStatus(id, target, statusUpdateDTO.getRemark());
        Delivery delivery = deliveryMapper.getById(existing.getDeliveryId());
        Delivery deliveryUpdate = new Delivery();
        deliveryUpdate.setId(delivery.getId());
        if (target == InterviewStatusConstant.FINISHED) {
            deliveryUpdate.setStatus(DeliveryStatusConstant.INTERVIEW_PASS);
            deliveryUpdate.setStatusRemark("面试通过");
        } else {
            deliveryUpdate.setStatus(DeliveryStatusConstant.UNFIT);
            deliveryUpdate.setStatusRemark("面试不通过");
        }
        deliveryUpdate.setUpdateTime(LocalDateTime.now());
        deliveryMapper.updateStatus(deliveryUpdate);

        Job job = jobMapper.getById(delivery.getJobId());
        notifyCandidate(delivery.getUserId(), job, id,
                target == InterviewStatusConstant.FINISHED
                        ? "面试通过，请留意后续招聘进展" : "本次面试未通过");
    }

    @Override
    @Transactional
    public void updateStatusForCandidate(Long id, StatusUpdateDTO statusUpdateDTO) {
        Interview existing = interviewMapper.getById(id);
        if (existing == null) {
            throw new BaseException("面试邀约不存在");
        }
        Delivery delivery = deliveryMapper.getById(existing.getDeliveryId());
        if (delivery == null || !BaseContext.getCurrentUserId().equals(delivery.getUserId())) {
            throw new BaseException("无权操作该面试邀约");
        }
        Integer target = statusUpdateDTO == null ? null : statusUpdateDTO.getStatus();
        if (existing.getStatus() != InterviewStatusConstant.PENDING
                || (target != InterviewStatusConstant.CONFIRMED
                && target != InterviewStatusConstant.REJECTED)) {
            throw new BaseException("当前面试邀约不能执行该操作");
        }

        String feedback = statusUpdateDTO.getRemark();
        interviewMapper.updateStatus(id, target, feedback);
        if (target == InterviewStatusConstant.REJECTED) {
            Delivery update = new Delivery();
            update.setId(delivery.getId());
            update.setStatus(DeliveryStatusConstant.REJECTED);
            update.setStatusRemark(feedback == null ? "求职者拒绝面试邀约" : feedback);
            update.setUpdateTime(LocalDateTime.now());
            deliveryMapper.updateStatus(update);
        }

        Job job = jobMapper.getById(delivery.getJobId());
        notifyHr(job, id, target == InterviewStatusConstant.CONFIRMED
                ? "求职者已接受面试邀约" : "求职者已拒绝面试邀约");
    }

    private Delivery requireHrDelivery(Long deliveryId) {
        if (deliveryId == null) {
            throw new BaseException("投递记录不能为空");
        }
        Delivery delivery = deliveryMapper.getById(deliveryId);
        if (delivery == null) {
            throw new BaseException("投递记录不存在");
        }
        Job job = jobMapper.getById(delivery.getJobId());
        ensureActiveCandidate(delivery);
        if (job == null || !BaseContext.getCurrentUserId().equals(job.getHrId())) {
            throw new BaseException("无权操作该投递记录");
        }
        return delivery;
    }

    private Interview requireHrInterview(Long id) {
        Interview interview = interviewMapper.getById(id);
        if (interview == null) {
            throw new BaseException("面试邀约不存在");
        }
        Delivery delivery = deliveryMapper.getById(interview.getDeliveryId());
        ensureActiveCandidate(delivery);
        Job job = delivery == null ? null : jobMapper.getById(delivery.getJobId());
        if (job == null || !BaseContext.getCurrentUserId().equals(job.getHrId())) {
            throw new BaseException("无权操作该面试邀约");
        }
        return interview;
    }

    private void ensureActiveCandidate(Delivery delivery) {
        if (delivery == null || delivery.getUserId() == null) {
            throw new BaseException("关联的求职者不存在");
        }
        com.recruit.entity.User user = userMapper.getById(delivery.getUserId());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BaseException("该求职者当前不可参与招聘流程");
        }
    }

    private void validateInterview(InterviewDTO dto) {
        if (dto == null || dto.getDeliveryId() == null || dto.getInterviewTime() == null
                || dto.getLocation() == null || dto.getLocation().trim().isEmpty()) {
            throw new BaseException("投递记录、面试时间和面试地点不能为空");
        }
    }

    private void notifyCandidate(Long userId, Job job, Long interviewId, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(2);
        notification.setTitle("面试邀约通知");
        notification.setContent("职位“" + (job == null ? "" : job.getTitle()) + "”："
                + content);
        notification.setBizId(interviewId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private void notifyHr(Job job, Long interviewId, String content) {
        if (job == null || job.getHrId() == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(job.getHrId());
        notification.setType(2);
        notification.setTitle("面试邀约状态更新");
        notification.setContent(content);
        notification.setBizId(interviewId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }
}
