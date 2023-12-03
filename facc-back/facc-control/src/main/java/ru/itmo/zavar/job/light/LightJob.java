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
import ru.itmo.zavar.repository.TimetableEntryRepository;
import ru.itmo.zavar.util.JobStatus;

import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
@DisallowConcurrentExecution
@Component
public class LightJob extends QuartzJobBean {
    @Autowired
    private TimetableEntryRepository timetableEntryRepository;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
        Long id = (Long) context.getMergedJobDataMap().get("id");
        TimetableEntryEntity timetableEntryEntity = timetableEntryRepository.findById(id).orElseThrow();
        timetableEntryEntity.setJobStatus(JobStatus.EXECUTING);
        log.info("Set {} status to job with id {}", JobStatus.EXECUTING, id);
        timetableEntryRepository.save(timetableEntryEntity);
        log.info("{} Start................", getClass().getName());
        IntStream.range(0, 10).forEach(i -> {
            log.info("Counting - {}", i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

        });
        log.info("{} End................", getClass().getName());
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
