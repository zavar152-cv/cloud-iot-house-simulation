package ru.itmo.zavar.service;

import ru.itmo.zavar.util.JobGroup;

import java.util.NoSuchElementException;

public interface SchedulerService {
    void createNewTimetableEntry(String name, JobGroup group, String cronExpression, String description);

    void updateTimetableEntry(Long id, String name, String cronExpression, String description) throws NoSuchElementException;

    void deleteTimetableEntry(Long id) throws NoSuchElementException;

    void pauseJob(Long id) throws NoSuchElementException;

    void resumeJob(Long id) throws NoSuchElementException, IllegalStateException;

    void startJob(Long id) throws NoSuchElementException, IllegalStateException;
}
