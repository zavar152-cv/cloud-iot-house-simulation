package ru.itmo.zavar.service;

import org.quartz.SchedulerException;
import ru.itmo.zavar.dto.TimetableEntryDTO;
import ru.itmo.zavar.util.JobGroup;

import java.util.List;
import java.util.NoSuchElementException;

public interface SchedulerService {
    void createTimetableEntry(String name, JobGroup group, String cronExpression, String description) throws SchedulerException, IllegalArgumentException;

    void updateTimetableEntry(Long id, String name, String cronExpression, String description) throws NoSuchElementException, SchedulerException;

    boolean deleteTimetableEntry(Long id) throws NoSuchElementException, SchedulerException;

    void pauseJob(Long id) throws NoSuchElementException, SchedulerException, IllegalStateException;

    void resumeJob(Long id) throws NoSuchElementException, IllegalStateException, SchedulerException;

    void startJob(Long id) throws NoSuchElementException, IllegalStateException, SchedulerException;

    List<TimetableEntryDTO.Response.TimetableEntry> getAllEntries();

    TimetableEntryDTO.Response.TimetableEntry getEntryById(Long id) throws NoSuchElementException;
}
