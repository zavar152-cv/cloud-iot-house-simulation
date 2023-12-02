package ru.itmo.zavar.job.curtains;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import ru.itmo.zavar.repository.TimetableEntryRepository;

import java.util.stream.IntStream;

@Slf4j
@DisallowConcurrentExecution
public class CurtainsJob extends QuartzJobBean {
    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
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
    }
}
