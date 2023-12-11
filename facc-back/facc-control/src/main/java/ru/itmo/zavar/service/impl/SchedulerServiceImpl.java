package ru.itmo.zavar.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.component.JobScheduleCreator;
import ru.itmo.zavar.dto.SimulationDTO;
import ru.itmo.zavar.dto.TimetableEntryDTO;
import ru.itmo.zavar.entity.*;
import ru.itmo.zavar.job.curtains.CurtainsJob;
import ru.itmo.zavar.job.light.LightJob;
import ru.itmo.zavar.job.music.MusicJob;
import ru.itmo.zavar.job.speakers.SpeakersJob;
import ru.itmo.zavar.model.JobGroup;
import ru.itmo.zavar.model.JobStatus;
import ru.itmo.zavar.repo.*;
import ru.itmo.zavar.service.SchedulerService;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "SchedulerService")
public class SchedulerServiceImpl implements SchedulerService {

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final TimetableEntryRepository timetableEntryRepository;
    private final DeviceRepository deviceRepository;
    private final ActionRepository actionRepository;
    private final ApplicationContext context;
    private final JobScheduleCreator scheduleCreator;
    private final StateRepository stateRepository;
    private final StatusRepository statusRepository;
    @Value("${status.enabled}")
    private String enabledStatus;
    @Value("${status.disabled}")
    private String disabledStatus;
    private boolean simulationEnabled = false;
    private static final String enableSimulationJobName = SimulationJob.class.getName() + "E";
    private static final String disableSimulationJobName = SimulationJob.class.getName() + "D";
    private static final String simulationJobGroup = SimulationJob.class.getName();

    @PostConstruct
    @Transactional
    public void loadAndScheduleTimetableEntries() throws SchedulerException {

        statusRepository.save(new StatusEntity(1L, enabledStatus));
        statusRepository.save(new StatusEntity(2L, disabledStatus));
        Optional<StateEntity> optionalStateEntity = stateRepository.findById(1L);

        if (optionalStateEntity.isEmpty()) {
            statusRepository.findById(2L).ifPresent(statusEntity -> {
                stateRepository.save(new StateEntity(1L, statusEntity, null, null));
            });
        } else {
            StateEntity stateEntity = optionalStateEntity.get();
            if (stateEntity.getSimulationStatus().getName().equals(disabledStatus)) {
                disableSimulation();
            } else if (stateEntity.getSimulationStatus().getName().equals(enabledStatus)) {
                if (stateEntity.getStartCronExpression() == null || stateEntity.getEndCronExpression() == null) {
                    enableSimulation();
                } else {
                    setSchedulerForSimulation(stateEntity.getStartCronExpression(), stateEntity.getEndCronExpression());
                }
            }
        }
    }


    @Override
    public void createTimetableEntry(String name, JobGroup group, String cronExpression, String description, String deviceId, Long actionId, List<String> arguments) throws SchedulerException, IllegalArgumentException, EntityNotFoundException {
        DeviceEntity deviceEntity = deviceRepository.findById(deviceId).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        ActionEntity actionEntity = actionRepository.findById(actionId).orElseThrow(() -> new EntityNotFoundException("Action not found"));
        Class<? extends QuartzJobBean> jobClass = getClassByGroup(group);

        if (actionEntity.getArgumentsCount() != arguments.size())
            throw new IllegalArgumentException("Check arguments count");

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
                        .device(deviceEntity)
                        .action(actionEntity)
                        .arguments(arguments)
                        .jobGroup(group).build();
                TimetableEntryEntity savedEntry = timetableEntryRepository.save(entryEntity);
                jobDetail = scheduleCreator.createJobForDevice(jobClass, false, context, name, group.name(), deviceEntity.getId(), savedEntry.getId());

                jobDetail.getJobDataMap().put("arguments", arguments);
                jobDetail.getJobDataMap().put("action", actionEntity.getId());

                Trigger trigger = scheduleCreator.createCronTrigger(name, new Date(),
                        cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("job {} with id {} scheduled", savedEntry.getName(), savedEntry.getId());
                if (!simulationEnabled) {
                    pauseJob(savedEntry.getId());
                }
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
            if (deleted) {
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
            if (timetableEntryEntity.getJobStatus().equals(JobStatus.PAUSED))
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
            if (!timetableEntryEntity.getJobStatus().equals(JobStatus.PAUSED))
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
            if (timetableEntryEntity.getJobStatus().equals(JobStatus.EXECUTING))
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
                entry.getCronExpression(), entry.getDescription(),
                entry.getDevice().getId(), entry.getAction().getId(),
                entry.getDevice().getName(), entry.getAction().getAction(),
                entry.getArguments())));
        return all;
    }

    @Override
    public TimetableEntryDTO.Response.TimetableEntry getEntryById(Long id) throws NoSuchElementException {
        TimetableEntryEntity entry = timetableEntryRepository.findById(id).orElseThrow();
        return new TimetableEntryDTO.Response.TimetableEntry(entry.getId(),
                entry.getName(), entry.getJobGroup(),
                entry.getCronExpression(), entry.getDescription(),
                entry.getDevice().getId(), entry.getAction().getId(),
                entry.getDevice().getName(), entry.getAction().getAction(),
                entry.getArguments());
    }

    @Override
    public void enableSimulation() {
        log.info(String.valueOf(simulationEnabled));
        if (simulationEnabled)
            return;
        statusRepository.findById(1L).ifPresent(statusEntity -> {
            stateRepository.findById(1L).ifPresent(stateEntity -> {
                stateEntity.setSimulationStatus(statusEntity);
                stateRepository.save(stateEntity);
            });
        });
        simulationEnabled = true;
        List<TimetableEntryDTO.Response.TimetableEntry> allEntries = getAllEntries();
        log.info("Loaded {} timetable entries", allEntries.size());
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        allEntries.forEach(timetableEntry -> {
            Class<? extends QuartzJobBean> jobClass = getClassByGroup(timetableEntry.getGroup());
            JobDetail jobDetail = scheduleCreator.createJobForDevice(jobClass, false, context, timetableEntry.getName(), timetableEntry.getGroup().name(), timetableEntry.getDeviceId(), timetableEntry.getId());

            jobDetail.getJobDataMap().put("arguments", timetableEntry.getArguments());
            jobDetail.getJobDataMap().put("action", timetableEntry.getActionId());

            Trigger trigger = scheduleCreator.createCronTrigger(timetableEntry.getName(), new Date(),
                    timetableEntry.getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
            try {
                scheduler.scheduleJob(jobDetail, trigger);
                timetableEntryRepository.findById(timetableEntry.getId()).ifPresent(entry -> {
                    entry.setJobStatus(JobStatus.SCHEDULED);
                    timetableEntryRepository.save(entry);
                });
            } catch (SchedulerException e) {
                log.error(e.getMessage(), e);
            }
            log.info("job {} with id {} scheduled", timetableEntry.getName(), timetableEntry.getId());
        });
        log.info("Simulation enabled");
    }

    @Override
    public void disableSimulation() {
        log.info(String.valueOf(simulationEnabled));
        if (!simulationEnabled)
            return;
        statusRepository.findById(2L).ifPresent(statusEntity -> {
            stateRepository.findById(1L).ifPresent(stateEntity -> {
                stateEntity.setSimulationStatus(statusEntity);
                stateRepository.save(stateEntity);
            });
        });
        simulationEnabled = false;
        Iterable<TimetableEntryEntity> all = timetableEntryRepository.findAll();
        all.forEach(entry -> {
            try {
                schedulerFactoryBean.getScheduler().deleteJob(new JobKey(entry.getName(),
                        entry.getJobGroup().name()));
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("Simulation disabled");
    }

    @Override
    public void setSchedulerForSimulation(String startCron, String endCron) throws SchedulerException {
        try {
            removeSchedulerForSimulation();
        } catch (SchedulerException e) {
            log.warn("Simulation schedule was null");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobDetail enableJobDetail = scheduleCreator.createJobForSimulation(SimulationJob.class, false, context,
                enableSimulationJobName, simulationJobGroup, true);
        JobDetail disableJobDetail = scheduleCreator.createJobForSimulation(SimulationJob.class, false, context,
                disableSimulationJobName, simulationJobGroup, false);

        Trigger enableTrigger = scheduleCreator.createCronTrigger(enableSimulationJobName, new Date(),
                startCron, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        Trigger disableTrigger = scheduleCreator.createCronTrigger(disableSimulationJobName, new Date(),
                endCron, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

        scheduler.scheduleJob(enableJobDetail, enableTrigger);
        scheduler.scheduleJob(disableJobDetail, disableTrigger);

        stateRepository.findById(1L).ifPresent(stateEntity -> {
            stateEntity.setStartCronExpression(startCron);
            stateEntity.setEndCronExpression(endCron);
            stateRepository.save(stateEntity);
        });
        log.info("Scheduled simulation with start cron: {} and end cron {}", startCron, endCron);
    }

    @Override
    public void removeSchedulerForSimulation() throws SchedulerException {
        disableSimulation();

        schedulerFactoryBean.getScheduler().deleteJob(new JobKey(enableSimulationJobName, simulationJobGroup));
        schedulerFactoryBean.getScheduler().deleteJob(new JobKey(disableSimulationJobName, simulationJobGroup));

        stateRepository.findById(1L).ifPresent(stateEntity -> {
            stateEntity.setStartCronExpression(null);
            stateEntity.setEndCronExpression(null);
            stateRepository.save(stateEntity);
        });
        log.info("Removed simulation schedule");
    }

    @Override
    public SimulationDTO.Response.GetSchedule getSchedulerForSimulation() throws NoSuchElementException {
        StateEntity stateEntity = stateRepository.findById(1L).orElseThrow();
        return new SimulationDTO.Response.GetSchedule(stateEntity.getStartCronExpression(), stateEntity.getEndCronExpression());
    }

    @DisallowConcurrentExecution
    @Component
    public static class SimulationJob extends QuartzJobBean {
        @Autowired
        private SchedulerService schedulerService;

        @Override
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
            Boolean enable = (Boolean) context.getMergedJobDataMap().get("enable");
            if (enable) {
                schedulerService.enableSimulation();
            } else {
                schedulerService.disableSimulation();
            }
        }
    }

}
