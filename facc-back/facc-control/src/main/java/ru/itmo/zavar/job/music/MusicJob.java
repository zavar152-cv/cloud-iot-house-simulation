package ru.itmo.zavar.job.music;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import ru.itmo.zavar.dto.ActionDTO;
import ru.itmo.zavar.entity.TimetableEntryEntity;
import ru.itmo.zavar.model.JobStatus;
import ru.itmo.zavar.mqtt.MqttSession;
import ru.itmo.zavar.repo.DeviceOnRepository;
import ru.itmo.zavar.repo.GroupOnRepository;
import ru.itmo.zavar.repo.TimetableEntryRepository;
import ru.itmo.zavar.service.ActionService;
import ru.itmo.zavar.service.CloudLoggingService;
import yandex.cloud.api.logging.v1.LogEntryOuterClass;

import java.util.List;
import java.util.Optional;

@Slf4j
@DisallowConcurrentExecution
@Component
public class MusicJob extends QuartzJobBean {
    @Autowired
    private TimetableEntryRepository timetableEntryRepository;
    @Autowired
    private DeviceOnRepository deviceOnRepository;
    @Autowired
    private CloudLoggingService cloudLoggingService;
    @Autowired
    private ActionService actionService;
    @Autowired
    private GroupOnRepository groupOnRepository;

    @Value("${yandex.mqtt.broker-url}")
    private String mqttBrokerUrl;

    @Value("${yandex.mqtt.registry-id}")
    private String mqttRegistryId;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
        Long id = (Long) context.getMergedJobDataMap().get("id");
        String deviceId = (String) context.getMergedJobDataMap().get("deviceId");
        TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
        if (groupOnRepository.findByJobGroup(timetableEntryEntity.getJobGroup()).isEmpty()) {
            return;
        }
        if (deviceOnRepository.findByDevice_Id(deviceId).isEmpty()) {
            return;
        }
        timetableEntryEntity.setJobStatus(JobStatus.EXECUTING);
        timetableEntryRepository.save(timetableEntryEntity);
        log.info("Set {} status to job with id {}", JobStatus.EXECUTING, id);
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Set {} status to job with id {}", JobStatus.EXECUTING, id);

        List<String> arguments = (List<String>) context.getMergedJobDataMap().get("arguments");
        Long actionId = (Long) context.getMergedJobDataMap().get("action");
        ActionDTO.Response.Action actionById = actionService.getActionById(actionId);
        String action = actionById.getName();
        if(actionById.getArgumentsCount() != arguments.size()) {
            log.error("Check error count in action {} with job id {}", action, id);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, getClass().getName(), "Check error count in action {} with job id {}", action, id);
            return;
        }
        if (arguments.isEmpty()) {
            log.info("Executing job with id {} with action {}", id, action);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Executing job with action {}", id, action);
        } else {
            log.info("Executing job with id {} with arguments {} and action {}", id, String.join(", ", arguments), action);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Executing job with arguments {} and action {}", id, String.join(", ", arguments), action);
        }
        sendToDevice(deviceId, action, arguments);

        Optional<TimetableEntryEntity> optionalTimetableEntry = timetableEntryRepository.findById(id);
        if (optionalTimetableEntry.isPresent()) {
            timetableEntryEntity = optionalTimetableEntry.get();
            if (timetableEntryEntity.getJobStatus().equals(JobStatus.PAUSED)) {
                log.warn("Job with id {} was paused while executing", id);
                cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.WARN, getClass().getName(), "Job with id {} was paused while executing", id);
            } else {
                timetableEntryEntity.setJobStatus(JobStatus.SCHEDULED);
                timetableEntryRepository.save(timetableEntryEntity);
                log.info("Set {} status to job with id {}", JobStatus.SCHEDULED, id);
                cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Set {} status to job with id {}", JobStatus.SCHEDULED, id);
            }
        } else {
            log.warn("Job with id {} was deleted before ending", id);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.WARN, getClass().getName(), "Job with id {} was deleted before ending", id);
        }
    }

    private void sendToDevice(String deviceId, String action, List<String> arguments) {
        log.info("Sending to device {} ..." , deviceId);
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Sending to device {} ..." , deviceId);
        try {
            MqttSession mqttSession = new MqttSession(mqttBrokerUrl, getClass().getSimpleName() + ":" + deviceId, mqttRegistryId);
            mqttSession.start();
            mqttSession.publish("$devices/" + deviceId + "/commands", action + " " + String.join(",", arguments));
            mqttSession.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
