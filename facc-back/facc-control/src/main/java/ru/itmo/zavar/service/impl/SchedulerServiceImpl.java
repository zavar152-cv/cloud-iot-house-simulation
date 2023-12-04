package ru.itmo.zavar.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.component.JobScheduleCreator;
import ru.itmo.zavar.dto.TimetableEntryDTO;
import ru.itmo.zavar.entity.TimetableEntryEntity;
import ru.itmo.zavar.job.curtains.CurtainsJob;
import ru.itmo.zavar.job.light.LightJob;
import ru.itmo.zavar.job.music.MusicJob;
import ru.itmo.zavar.job.speakers.SpeakersJob;
import ru.itmo.zavar.repository.TimetableEntryRepository;
import ru.itmo.zavar.service.SchedulerService;
import ru.itmo.zavar.util.JobGroup;
import ru.itmo.zavar.util.JobStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "SchedulerService")
public class SchedulerServiceImpl implements SchedulerService {

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final TimetableEntryRepository timetableEntryRepository;
    private final ApplicationContext context;
    private final JobScheduleCreator scheduleCreator;

    @PostConstruct
    public void loadAndScheduleTimetableEntries() {
        List<TimetableEntryDTO.Response.TimetableEntry> allEntries = getAllEntries();
        log.info("Loaded {} timetable entries", allEntries.size());
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        allEntries.forEach(timetableEntry -> {
            Class<? extends QuartzJobBean> jobClass = getClassByGroup(timetableEntry.getGroup());
            JobDetail jobDetail = scheduleCreator.createJob(jobClass, false, context, timetableEntry.getName(), timetableEntry.getGroup().name(), timetableEntry.getId());

            Trigger trigger = scheduleCreator.createCronTrigger(timetableEntry.getName(), new Date(),
                    timetableEntry.getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                log.error(e.getMessage(), e);
            }
            log.info("job {} with id {} scheduled", timetableEntry.getName(), timetableEntry.getId());
        });
    }

    @Override
    public void createTimetableEntry(String name, JobGroup group, String cronExpression, String description) throws SchedulerException, IllegalArgumentException {
        Class<? extends QuartzJobBean> jobClass = getClassByGroup(group);
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
                log.info("job {} with id {} scheduled", savedEntry.getName(), savedEntry.getId());
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
                throw new IllegalArgumentException("Job is already exists");
            }
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private Class<? extends QuartzJobBean> getClassByGroup(JobGroup jobGroup) {
        Class<? extends QuartzJobBean> jobClass = null;
        switch (jobGroup) {
            case LIGHT_GROUP -> jobClass = LightJob.class;
            case CURTAINS_GROUP -> jobClass = CurtainsJob.class;
            case MUSIC_GROUP -> jobClass = MusicJob.class;
            case SPEAKERS_GROUP -> jobClass = SpeakersJob.class;
        }
        return jobClass;
    }

    @Override
    public void updateTimetableEntry(Long id, String name, String cronExpression, String description) throws NoSuchElementException, SchedulerException {
        TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();

        Trigger newTrigger = scheduleCreator.createCronTrigger(name, new Date(),
                cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        try {
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(timetableEntryEntity.getName()), newTrigger);
            timetableEntryEntity.setName(name);
            timetableEntryEntity.setCronExpression(cronExpression);
            timetableEntryEntity.setDescription(description);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info("job with id {} updated", timetableEntryEntity.getId());
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteTimetableEntry(Long id) throws NoSuchElementException, SchedulerException {
        try {
            TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
            boolean deleted = schedulerFactoryBean.getScheduler().deleteJob(new JobKey(timetableEntryEntity.getName(),
                    timetableEntryEntity.getJobGroup().name()));
            if(deleted) {
                timetableEntryRepository.deleteById(id);
                log.info("job {} with id {} deleted", timetableEntryEntity.getName(), timetableEntryEntity.getId());
            } else {
                log.info("job {} with id {} can't be deleted", timetableEntryEntity.getName(), timetableEntryEntity.getId());
            }
            return deleted;
        } catch (SchedulerException e) {
            log.error("Failed to delete job with id - {}", id, e);
            throw e;
        }
    }

    @Override
    public void pauseJob(Long id) throws NoSuchElementException, SchedulerException, IllegalStateException {
        try {
            TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
            if(timetableEntryEntity.getJobStatus().equals(JobStatus.PAUSED))
                throw new IllegalStateException("Job was paused yet");
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(timetableEntryEntity.getName(),
                    timetableEntryEntity.getJobGroup().name()));
            timetableEntryEntity.setJobStatus(JobStatus.PAUSED);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info("job {} with id {} paused", timetableEntryEntity.getName(), timetableEntryEntity.getId());
        } catch (SchedulerException e) {
            log.error("Failed to pause job with id - {}", id, e);
            throw e;
        }
    }

    @Override
    public void resumeJob(Long id) throws NoSuchElementException, IllegalStateException, SchedulerException {
        try {
            TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
            if(!timetableEntryEntity.getJobStatus().equals(JobStatus.PAUSED))
                throw new IllegalStateException("Job isn't paused now");
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(timetableEntryEntity.getName(),
                    timetableEntryEntity.getJobGroup().name()));
            timetableEntryEntity.setJobStatus(JobStatus.SCHEDULED);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info("job {} with id {} resumed", timetableEntryEntity.getName(), timetableEntryEntity.getId());
        } catch (SchedulerException e) {
            log.error("Failed to resume job with id - {}", id, e);
            throw e;
        }
    }

    @Override
    public void startJob(Long id) throws NoSuchElementException, IllegalStateException, SchedulerException {
        try {
            TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
            if(timetableEntryEntity.getJobStatus().equals(JobStatus.EXECUTING))
                throw new IllegalStateException("Job is executing now");
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(timetableEntryEntity.getName(),
                    timetableEntryEntity.getJobGroup().name()));
            timetableEntryEntity.setJobStatus(JobStatus.EXECUTING);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info("job {} with id {} started now", timetableEntryEntity.getName(), timetableEntryEntity.getId());
        } catch (SchedulerException e) {
            log.error("Failed to start new job with id - {}", id, e);
            throw e;
        }
    }

    @Override
    public List<TimetableEntryDTO.Response.TimetableEntry> getAllEntries() {
        Iterable<TimetableEntryEntity> iterable = timetableEntryRepository.findAll();
        List<TimetableEntryDTO.Response.TimetableEntry> all = new ArrayList<>();
        iterable.forEach(entry -> all.add(new TimetableEntryDTO.Response.TimetableEntry(entry.getId(),
                entry.getName(), entry.getJobGroup(),
                entry.getCronExpression(), entry.getDescription())));
        return all;
    }

    @Override
    public TimetableEntryDTO.Response.TimetableEntry getEntryById(Long id) throws NoSuchElementException {
        TimetableEntryEntity entry = timetableEntryRepository.findById(id).orElseThrow();
        return new TimetableEntryDTO.Response.TimetableEntry(entry.getId(),
                entry.getName(), entry.getJobGroup(),
                entry.getCronExpression(), entry.getDescription());
    }
}
