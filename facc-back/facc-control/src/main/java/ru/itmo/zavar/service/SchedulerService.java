package ru.itmo.zavar.service;

import jakarta.persistence.EntityNotFoundException;
import org.quartz.SchedulerException;
import ru.itmo.zavar.dto.SimulationDTO;
import ru.itmo.zavar.dto.TimetableEntryDTO;
import ru.itmo.zavar.model.JobGroup;

import java.util.List;
import java.util.NoSuchElementException;

public interface SchedulerService {
    void createTimetableEntry(String name, JobGroup group, String cronExpression, String description, String deviceId, Long actionId, List<String> arguments) throws SchedulerException, IllegalArgumentException, EntityNotFoundException;

    void createTimetableEntryForGroup(String name, JobGroup group, String cronExpression, String description, Long actionId, List<String> arguments) throws SchedulerException, IllegalArgumentException, EntityNotFoundException;

    void updateTimetableEntry(Long id, String name, String cronExpression, String description, List<String> arguments) throws NoSuchElementException, SchedulerException;

    boolean deleteTimetableEntry(Long id) throws NoSuchElementException, SchedulerException;

    void pauseJob(Long id) throws NoSuchElementException, SchedulerException, IllegalStateException;

    void resumeJob(Long id) throws NoSuchElementException, IllegalStateException, SchedulerException;

    void startJob(Long id) throws NoSuchElementException, IllegalStateException, SchedulerException;

    List<TimetableEntryDTO.Response.TimetableEntry> getAllEntries();

    TimetableEntryDTO.Response.TimetableEntry getEntryById(Long id) throws NoSuchElementException;

    void enableSimulation();

    void disableSimulation();

    void addSchedulerForSimulation(String name, String startCron, String endCron) throws SchedulerException, IllegalArgumentException;

    void updateSchedulerForSimulation(Long id, String startCron, String endCron) throws SchedulerException, NoSuchElementException;

    void removeSchedulerForSimulation(Long id) throws SchedulerException, NoSuchElementException;

    List<SimulationDTO.Response.GetSimulationSchedule> getAllSimulationSchedule();

    SimulationDTO.Response.GetSimulationInfo getSimulationInfo() throws NoSuchElementException;

    SimulationDTO.Response.GetSimulationSchedule getSimulationScheduleById(Long id) throws NoSuchElementException;
}
