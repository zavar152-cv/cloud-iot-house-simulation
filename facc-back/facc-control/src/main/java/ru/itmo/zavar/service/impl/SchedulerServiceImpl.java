package ru.itmo.zavar.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
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
import ru.itmo.zavar.service.CloudLoggingService;
import ru.itmo.zavar.service.SchedulerService;
import yandex.cloud.api.logging.v1.LogEntryOuterClass;

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
    private final SimulationTimetableRepository simulationTimetableRepository;
    private final CloudLoggingService cloudLoggingService;
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
                stateRepository.save(new StateEntity(1L, statusEntity));
            });
        } else {
            StateEntity stateEntity = optionalStateEntity.get();
            if (stateEntity.getSimulationStatus().getName().equals(disabledStatus)) {
                disableSimulation();
            } else if (stateEntity.getSimulationStatus().getName().equals(enabledStatus)) {
                if (simulationTimetableRepository.count() == 0) {
                    enableSimulation();
                } else {
                    loadAllSimulationSchedule();
                    enableSimulation();
                }
            }
        }
    }

    private void loadAllSimulationSchedule() {
        simulationTimetableRepository.findAll().forEach(entity -> {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail enableJobDetail = scheduleCreator.createJobForSimulation(SimulationJob.class, false, context,
                    entity.getName() + enableSimulationJobName, simulationJobGroup, true);
            JobDetail disableJobDetail = scheduleCreator.createJobForSimulation(SimulationJob.class, false, context,
                    entity.getName() + disableSimulationJobName, simulationJobGroup, false);

            Trigger enableTrigger = scheduleCreator.createCronTrigger(entity.getName() + enableSimulationJobName, new Date(),
                    entity.getStartCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
            Trigger disableTrigger = scheduleCreator.createCronTrigger(entity.getName() + disableSimulationJobName, new Date(),
                    entity.getEndCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

            try {
                scheduler.scheduleJob(enableJobDetail, enableTrigger);
                scheduler.scheduleJob(disableJobDetail, disableTrigger);
                log.info("Scheduled simulation with start cron: {} and end cron {}", entity.getStartCronExpression(), entity.getEndCronExpression());
                cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Scheduled simulation with start cron: {} and end cron {}", entity.getStartCronExpression(), entity.getEndCronExpression());
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
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
                log.info("job {} with id {} saved", savedEntry.getName(), savedEntry.getId());
                cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} saved", savedEntry.getName(), savedEntry.getId());
                jobDetail = scheduleCreator.createJobForDevice(jobClass, false, context, name, group.name(), deviceEntity.getId(), savedEntry.getId());

                jobDetail.getJobDataMap().put("arguments", arguments);
                jobDetail.getJobDataMap().put("action", actionEntity.getId());

                Trigger trigger = scheduleCreator.createCronTrigger(name, new Date(),
                        cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                if (simulationEnabled) {
                    scheduler.scheduleJob(jobDetail, trigger);
                    log.info("job {} with id {} scheduled", savedEntry.getName(), savedEntry.getId());
                    cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} scheduled", savedEntry.getName(), savedEntry.getId());
                }
            } else {
                log.error("Job is already exists");
                cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, getClass().getName(), "Job is already exists", new IllegalArgumentException("Job is already exists"));
                throw new IllegalArgumentException("Job is already exists");
            }
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void createTimetableEntryForGroup(String name, JobGroup group, String cronExpression, String description, Long actionId, List<String> arguments) throws SchedulerException, IllegalArgumentException, EntityNotFoundException {
        List<DeviceEntity> allByJobGroup = deviceRepository.findAllByJobGroup(group);
        int i = 0;
        for (DeviceEntity deviceEntity : allByJobGroup) {
            createTimetableEntry(name + i, group, cronExpression, description, deviceEntity.getId(), actionId, arguments);
            i++;
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
    public void updateTimetableEntry(Long id, String name, String cronExpression, String description, List<String> arguments) throws NoSuchElementException, SchedulerException {
        TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();

        try {
            schedulerFactoryBean.getScheduler().deleteJob(new JobKey(timetableEntryEntity.getName(),
                    timetableEntryEntity.getJobGroup().name()));
            timetableEntryEntity.setName(name);
            timetableEntryEntity.setCronExpression(cronExpression);
            timetableEntryEntity.setDescription(description);
            timetableEntryEntity.setArguments(arguments);
            timetableEntryRepository.save(timetableEntryEntity);
            log.info("job with id {} updated", timetableEntryEntity.getId());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job with id {} updated", timetableEntryEntity.getId());

            Class<? extends QuartzJobBean> jobClass = getClassByGroup(timetableEntryEntity.getJobGroup());

            JobDetail jobDetail = scheduleCreator.createJobForDevice(jobClass, false, context, name, timetableEntryEntity.getJobGroup().name(), timetableEntryEntity.getDevice().getId(), timetableEntryEntity.getId());

            jobDetail.getJobDataMap().put("arguments", timetableEntryEntity.getArguments());
            jobDetail.getJobDataMap().put("action", timetableEntryEntity.getAction().getId());

            Trigger trigger = scheduleCreator.createCronTrigger(name, new Date(),
                    cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
            if (simulationEnabled) {
                Scheduler scheduler = schedulerFactoryBean.getScheduler();
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("job {} with id {} scheduled", timetableEntryEntity.getName(), timetableEntryEntity.getId());
                cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} scheduled", timetableEntryEntity.getName(), timetableEntryEntity.getId());
            }

        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, getClass().getName(), e.getMessage(), e);
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
                cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} deleted", timetableEntryEntity.getName(), timetableEntryEntity.getId());
            } else {
                log.info("job {} with id {} can't be deleted", timetableEntryEntity.getName(), timetableEntryEntity.getId());
                cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} can't be deleted", timetableEntryEntity.getName(), timetableEntryEntity.getId());
            }
            return deleted;
        } catch (SchedulerException e) {
            log.error("Failed to delete job with id - {}", id, e);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, getClass().getName(), "Failed to delete new job with id - " + id, e);
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
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} paused", timetableEntryEntity.getName(), timetableEntryEntity.getId());
        } catch (SchedulerException e) {
            log.error("Failed to pause job with id - {}", id, e);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, getClass().getName(), "Failed to pause new job with id - " + id, e);
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
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} resumed", timetableEntryEntity.getName(), timetableEntryEntity.getId());
        } catch (SchedulerException e) {
            log.error("Failed to resume job with id - {}", id, e);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, getClass().getName(), "Failed to resume new job with id - " + id, e);
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
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} started now", timetableEntryEntity.getName(), timetableEntryEntity.getId());
        } catch (SchedulerException e) {
            log.error("Failed to start new job with id - {}", id, e);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, getClass().getName(), "Failed to start new job with id - " + id, e);
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
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Loaded {} timetable entries", allEntries.size());
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
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "job {} with id {} scheduled", timetableEntry.getName(), timetableEntry.getId());
        });
        log.info("Simulation enabled");
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Simulation enabled");
    }

    @Override
    public void disableSimulation() {
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
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Simulation disabled");
    }

    @Override
    public void addSchedulerForSimulation(String name, String startCron, String endCron) throws SchedulerException, IllegalArgumentException {

        simulationTimetableRepository.save(SimulationTimetableEntity.builder()
                .startCronExpression(startCron)
                .endCronExpression(endCron)
                .name(name).build());

        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        JobDetail jobDetailEnable = JobBuilder
                .newJob(SimulationJob.class)
                .withIdentity(name, name + enableSimulationJobName).build();
        JobDetail jobDetailDisable = JobBuilder
                .newJob(SimulationJob.class)
                .withIdentity(name, name + disableSimulationJobName).build();
        if (!scheduler.checkExists(jobDetailEnable.getKey()) && !scheduler.checkExists(jobDetailDisable.getKey())) {
            JobDetail enableJobDetail = scheduleCreator.createJobForSimulation(SimulationJob.class, false, context,
                    name + enableSimulationJobName, simulationJobGroup, true);
            JobDetail disableJobDetail = scheduleCreator.createJobForSimulation(SimulationJob.class, false, context,
                    name + disableSimulationJobName, simulationJobGroup, false);

            Trigger enableTrigger = scheduleCreator.createCronTrigger(name + enableSimulationJobName, new Date(),
                    startCron, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
            Trigger disableTrigger = scheduleCreator.createCronTrigger(name + disableSimulationJobName, new Date(),
                    endCron, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

            scheduler.scheduleJob(enableJobDetail, enableTrigger);
            scheduler.scheduleJob(disableJobDetail, disableTrigger);

            log.info("Scheduled simulation with start cron: {} and end cron {}", startCron, endCron);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Scheduled simulation with start cron: {} and end cron {}", startCron, endCron);
        } else {
            throw new IllegalArgumentException("Job is already exists");
        }
    }

    @Override
    public void updateSchedulerForSimulation(Long id, String startCron, String endCron) throws SchedulerException, NoSuchElementException {
        SimulationTimetableEntity simulationTimetableEntity = simulationTimetableRepository.findById(id).orElseThrow();
        String name = simulationTimetableEntity.getName();
        removeSchedulerForSimulation(id);
        addSchedulerForSimulation(name, startCron, endCron);
    }

    @Override
    public void removeSchedulerForSimulation(Long id) throws SchedulerException, NoSuchElementException {
        SimulationTimetableEntity simulationTimetableEntity = simulationTimetableRepository.findById(id).orElseThrow();

        schedulerFactoryBean.getScheduler().deleteJob(new JobKey(simulationTimetableEntity.getName() + enableSimulationJobName, simulationJobGroup));
        schedulerFactoryBean.getScheduler().deleteJob(new JobKey(simulationTimetableEntity.getName() + disableSimulationJobName, simulationJobGroup));

        simulationTimetableRepository.deleteById(id);
        log.info("Removed simulation schedule");
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Removed simulation schedule");
    }

    @Override
    public List<SimulationDTO.Response.GetSimulationSchedule> getAllSimulationSchedule() {
        Iterable<SimulationTimetableEntity> iterable = simulationTimetableRepository.findAll();
        List<SimulationDTO.Response.GetSimulationSchedule> all = new ArrayList<>();
        iterable.forEach(entity -> {
            all.add(new SimulationDTO.Response.GetSimulationSchedule(entity.getId(), entity.getName(), entity.getStartCronExpression(), entity.getEndCronExpression()));
        });
        return all;
    }

    @Override
    public SimulationDTO.Response.GetSimulationSchedule getSimulationScheduleById(Long id) throws NoSuchElementException {
        SimulationTimetableEntity entity = simulationTimetableRepository.findById(id).orElseThrow();
        return new SimulationDTO.Response.GetSimulationSchedule(entity.getId(), entity.getName(), entity.getStartCronExpression(), entity.getEndCronExpression());
    }

    @Override
    public SimulationDTO.Response.GetSimulationInfo getSimulationInfo() throws NoSuchElementException {
        StateEntity stateEntity = stateRepository.findById(1L).orElseThrow();
        return new SimulationDTO.Response.GetSimulationInfo(stateEntity.getSimulationStatus().getName());
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
