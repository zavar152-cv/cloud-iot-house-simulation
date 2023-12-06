package ru.itmo.zavar.service;

import jakarta.persistence.EntityNotFoundException;
import org.quartz.SchedulerException;
import ru.itmo.zavar.dto.TimetableEntryDTO;
import ru.itmo.zavar.model.JobGroup;

import java.util.List;
import java.util.NoSuchElementException;

public interface SchedulerService {
    void createTimetableEntry(String name, JobGroup group, String cronExpression, String description, String deviceId, Long actionId, List<String> arguments) throws SchedulerException, IllegalArgumentException, EntityNotFoundException;

    void updateTimetableEntry(Long id, String name, String cronExpression, String description) throws NoSuchElementException, SchedulerException;

    boolean deleteTimetableEntry(Long id) throws NoSuchElementException, SchedulerException;

    void pauseJob(Long id) throws NoSuchElementException, SchedulerException, IllegalStateException;

    void resumeJob(Long id) throws NoSuchElementException, IllegalStateException, SchedulerException;

    void startJob(Long id) throws NoSuchElementException, IllegalStateException, SchedulerException;

    List<TimetableEntryDTO.Response.TimetableEntry> getAllEntries();

    TimetableEntryDTO.Response.TimetableEntry getEntryById(Long id) throws NoSuchElementException;
}
