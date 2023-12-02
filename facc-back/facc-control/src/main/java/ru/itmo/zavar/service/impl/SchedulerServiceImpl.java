package ru.itmo.zavar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.component.JobScheduleCreator;
import ru.itmo.zavar.entity.TimetableEntryEntity;
import ru.itmo.zavar.job.curtains.CurtainsJob;
import ru.itmo.zavar.job.light.LightJob;
import ru.itmo.zavar.job.music.MusicJob;
import ru.itmo.zavar.job.speakers.SpeakersJob;
import ru.itmo.zavar.repository.TimetableEntryRepository;
import ru.itmo.zavar.service.SchedulerService;
import ru.itmo.zavar.util.JobGroup;
import ru.itmo.zavar.util.JobStatus;

import java.util.Date;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final TimetableEntryRepository timetableEntryRepository;
    private final ApplicationContext context;
    private final JobScheduleCreator scheduleCreator;

    @Override
    @Transactional
    public void createNewTimetableEntry(String name, JobGroup group, String cronExpression, String description) {
        Class<? extends QuartzJobBean> jobClass = null;
        switch (group) {
            case LIGHT_GROUP -> jobClass = LightJob.class;
            case CURTAINS_GROUP -> jobClass = CurtainsJob.class;
            case MUSIC_GROUP -> jobClass = MusicJob.class;
            case SPEAKERS_GROUP -> jobClass = SpeakersJob.class;
        }
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobDetail jobDetail = JobBuilder
                    .newJob(jobClass)
                    .withIdentity(name, group.name()).build();
            if (!scheduler.checkExists(jobDetail.getKey())) {
                TimetableEntryEntity entryEntity = TimetableEntryEntity.builder()
                        .className(jobClass.getName())
                        .jobStatus(JobStatus.SCHEDULED)
                        .cronExpression(cronExpression)
                        .description(description)
                        .name(name)
                        .jobGroup(group).build();
                TimetableEntryEntity savedEntry = timetableEntryRepository.save(entryEntity);
                jobDetail = scheduleCreator.createJob(jobClass, false, context, name, group.name(), savedEntry.getId());

                Trigger trigger = scheduleCreator.createCronTrigger(name, new Date(),
                        cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                scheduler.scheduleJob(jobDetail, trigger);
                log.info(">>>>> jobName = [" + name + "]" + " scheduled.");
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
            }
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    //TODO stop job before update
    @Override
    public void updateTimetableEntry(Long id, String name, String cronExpression, String description) throws NoSuchElementException {
        TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();

        Trigger newTrigger = scheduleCreator.createCronTrigger(name, new Date(),
                cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        try {
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(timetableEntryEntity.getName()), newTrigger);
            timetableEntryEntity.setName(name);
            timetableEntryEntity.setCronExpression(cronExpression);
            timetableEntryEntity.setDescription(description);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info(">>>>> jobName = [" + name + "]" + " updated and scheduled.");
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    //TODO return boolean
    @Override
    public void deleteTimetableEntry(Long id) throws NoSuchElementException {
        try {
            TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
            schedulerFactoryBean.getScheduler().deleteJob(new JobKey(timetableEntryEntity.getName(), timetableEntryEntity.getJobGroup().name()));
            timetableEntryRepository.deleteById(id);
            log.info(">>>>> jobName = [" + timetableEntryEntity.getName() + "]" + " deleted.");
        } catch (SchedulerException e) {
            log.error("Failed to delete job with id - {}", id, e);
        }
    }

    @Override
    public void pauseJob(Long id) throws NoSuchElementException {
        try {
            TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(timetableEntryEntity.getName(), timetableEntryEntity.getJobGroup().name()));
            timetableEntryEntity.setJobStatus(JobStatus.PAUSED);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info(">>>>> jobName = [" + timetableEntryEntity.getName() + "]" + " paused.");
        } catch (SchedulerException e) {
            log.error("Failed to pause job with id - {}", id, e);
        }
    }

    @Override
    public void resumeJob(Long id) throws NoSuchElementException, IllegalStateException {
        try {
            TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
            if(!timetableEntryEntity.getJobStatus().equals(JobStatus.PAUSED))
                throw new IllegalStateException();
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(timetableEntryEntity.getName(), timetableEntryEntity.getJobGroup().name()));
            timetableEntryEntity.setJobStatus(JobStatus.EXECUTING);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info(">>>>> jobName = [" + timetableEntryEntity.getName() + "]" + " resumed.");
        } catch (SchedulerException e) {
            log.error("Failed to resume job with id - {}", id, e);
        }
    }

    @Override
    public void startJob(Long id) throws NoSuchElementException {
        try {
            TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
            if(timetableEntryEntity.getJobStatus().equals(JobStatus.EXECUTING))
                throw new IllegalStateException();
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(timetableEntryEntity.getName(), timetableEntryEntity.getJobGroup().name()));
            timetableEntryEntity.setJobStatus(JobStatus.EXECUTING);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info(">>>>> jobName = [" + timetableEntryEntity.getName() + "]" + " scheduled and started now.");
        } catch (SchedulerException e) {
            log.error("Failed to start new job with id - {}", id, e);
        }
    }
}
