package ru.itmo.zavar.job.light;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import ru.itmo.zavar.entity.TimetableEntryEntity;
import ru.itmo.zavar.repo.DeviceOnRepository;
import ru.itmo.zavar.repo.TimetableEntryRepository;
import ru.itmo.zavar.model.JobStatus;

import java.util.List;
import java.util.Optional;

@Slf4j
@DisallowConcurrentExecution
@Component
public class LightJob extends QuartzJobBean {
    @Autowired
    private TimetableEntryRepository timetableEntryRepository;
    @Autowired
    private DeviceOnRepository deviceOnRepository;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
        Long id = (Long) context.getMergedJobDataMap().get("id");
        String deviceId = (String) context.getMergedJobDataMap().get("deviceId");
        if(deviceOnRepository.findByDevice_Id(deviceId).isEmpty()) {
            return;
        }
        TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
        timetableEntryEntity.setJobStatus(JobStatus.EXECUTING);
        log.info("Set {} status to job with id {}", JobStatus.EXECUTING, id);
        timetableEntryRepository.save(timetableEntryEntity);

        List<String> arguments = (List<String>) context.getMergedJobDataMap().get("arguments");
        String action = (String) context.getMergedJobDataMap().get("action");
        if(arguments.isEmpty())
            log.info("Executing job with action {}", action);
        else
            log.info("Executing job with arguments {} and action {}", String.join(", ", arguments), action);

        Optional<TimetableEntryEntity> optionalTimetableEntry = timetableEntryRepository.findById(id);
        if(optionalTimetableEntry.isPresent()) {
            timetableEntryEntity = optionalTimetableEntry.get();
            if(timetableEntryEntity.getJobStatus().equals(JobStatus.PAUSED)) {
                log.warn("Job with id {} was paused while executing", id);
            } else {
                timetableEntryEntity.setJobStatus(JobStatus.SCHEDULED);
                timetableEntryRepository.save(timetableEntryEntity);
                log.info("Set {} status to job with id {}", JobStatus.SCHEDULED, id);
            }
        } else {
            log.warn("Job with id {} was deleted before ending", id);
        }
    }
}
