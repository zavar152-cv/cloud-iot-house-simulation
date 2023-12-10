package ru.itmo.zavar.job.speakers;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import ru.itmo.zavar.dto.ActionDTO;
import ru.itmo.zavar.dto.CommandForActionDTO;
import ru.itmo.zavar.entity.ActionEntity;
import ru.itmo.zavar.entity.CommandForActionEntity;
import ru.itmo.zavar.entity.TimetableEntryEntity;
import ru.itmo.zavar.model.JobStatus;
import ru.itmo.zavar.repo.*;
import ru.itmo.zavar.service.ActionService;
import ru.itmo.zavar.service.CloudLoggingService;
import ru.itmo.zavar.service.CommandForActionService;
import ru.itmo.zavar.service.SpeechKitService;
import yandex.cloud.api.logging.v1.LogEntryOuterClass;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@DisallowConcurrentExecution
@Component
public class SpeakersJob extends QuartzJobBean {
    @Autowired
    private TimetableEntryRepository timetableEntryRepository;
    @Autowired
    private DeviceOnRepository deviceOnRepository;
    @Autowired
    private CloudLoggingService cloudLoggingService;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private ActionRepository actionRepository;
    @Autowired
    private CommandForActionRepository commandForActionRepository;
    @Autowired
    private SpeechKitService speechKitService;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
        Long id = (Long) context.getMergedJobDataMap().get("id");
        String deviceId = (String) context.getMergedJobDataMap().get("deviceId");
        if (deviceOnRepository.findByDevice_Id(deviceId).isEmpty()) {
            return;
        }
        TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
        timetableEntryEntity.setJobStatus(JobStatus.EXECUTING);
        timetableEntryRepository.save(timetableEntryEntity);
        log.info("Set {} status to job with id {}", JobStatus.EXECUTING, id);
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, getClass().getName(), "Set {} status to job with id {}", JobStatus.EXECUTING, id);

        List<String> arguments = (List<String>) context.getMergedJobDataMap().get("arguments");
        Long actionId = (Long) context.getMergedJobDataMap().get("action");
        ActionEntity actionById = actionRepository.findById(actionId).orElseThrow();
        String action = actionById.getAction();
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
        try {
            executeAction(deviceId, action, arguments);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }

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

    private void executeAction(String deviceId, String action, List<String> arguments) throws UnsupportedAudioFileException, IOException {
        switch (action) {
            case "play_voice_command" -> {
                Long actionToPlayId = Long.parseLong(arguments.get(0));
                CommandForActionEntity commandForActionEntity = commandForActionRepository.findByAction_Id(actionToPlayId).orElseThrow();
                byte[] content;
                if(commandForActionEntity.getFile() == null) {
                    content = speechKitService.synthesize(commandForActionEntity.getCommand());
                } else {
                    content = commandForActionEntity.getFile().getContent();
                }
                sendToDevice(deviceId, action, Collections.emptyList(), content);
            }
            default -> sendToDevice(deviceId, action, arguments, new byte[0]);
        }
    }

    private void sendToDevice(String deviceId, String action, List<String> arguments, byte[] content) {
        log.info("Sending to device...");
    }
}
