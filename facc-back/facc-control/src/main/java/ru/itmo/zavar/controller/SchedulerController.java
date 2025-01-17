package ru.itmo.zavar.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.dto.SimulationDTO;
import ru.itmo.zavar.dto.TimetableEntryDTO;
import ru.itmo.zavar.service.SchedulerService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/scheduler")
@RequiredArgsConstructor
@Slf4j(topic = "SchedulerController")
public class SchedulerController {
    private final SchedulerService schedulerService;
    @Value("${status.enabled}")
    private String enabledStatus;
    @Value("${status.disabled}")
    private String disabledStatus;

    @PutMapping("/simulation")
    public ResponseEntity<?> changeState(@RequestParam("state") String state) {
        if (state.equals(enabledStatus)) {
            schedulerService.enableSimulation();
            return ResponseEntity.ok().build();
        } else if (state.equals(disabledStatus)) {
            schedulerService.disableSimulation();
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/simulation/schedule")
    public ResponseEntity<?> addScheduleForSimulation(@Valid @RequestBody SimulationDTO.Request.AddSchedule addSchedule) {
        try {
            schedulerService.addSchedulerForSimulation(addSchedule.getName(), addSchedule.getStartCronExpression(), addSchedule.getEndCronExpression());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (SchedulerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping("/simulation/schedule/{id}")
    public ResponseEntity<?> updateScheduleForSimulation(@PathVariable @Positive @NotNull Long id, @Valid @RequestBody SimulationDTO.Request.UpdateSchedule updateSchedule) {
        try {
            schedulerService.updateSchedulerForSimulation(id, updateSchedule.getStartCronExpression(), updateSchedule.getEndCronExpression());
            return ResponseEntity.ok().build();
        } catch (SchedulerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/simulation/schedule/{id}")
    public ResponseEntity<?> addScheduleForSimulation(@PathVariable @Positive @NotNull Long id) {
        try {
            schedulerService.removeSchedulerForSimulation(id);
            return ResponseEntity.ok().build();
        } catch (SchedulerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/simulation/schedule")
    public ResponseEntity<List<SimulationDTO.Response.GetSimulationSchedule>> getAllScheduleForSimulation() {
        List<SimulationDTO.Response.GetSimulationSchedule> allSimulationSchedule = schedulerService.getAllSimulationSchedule();
        return ResponseEntity.ok(allSimulationSchedule);
    }

    @GetMapping("/simulation/state")
    public ResponseEntity<SimulationDTO.Response.GetSimulationInfo> getSimulationInfo() {
        try {
            SimulationDTO.Response.GetSimulationInfo schedulerForSimulation = schedulerService.getSimulationInfo();
            return ResponseEntity.ok(schedulerForSimulation);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/timetable-entries")
    public ResponseEntity<List<TimetableEntryDTO.Response.TimetableEntry>> getAllTimetableEntries() {
        var all = schedulerService.getAllEntries();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/timetable-entries/{id}")
    public ResponseEntity<TimetableEntryDTO.Response.TimetableEntry> getTimetableEntry(@PathVariable @Positive @NotNull Long id) {
        try {
            TimetableEntryDTO.Response.TimetableEntry entry = schedulerService.getEntryById(id);
            return ResponseEntity.ok(entry);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/timetable-entries")
    public ResponseEntity<?> createTimetableEntry(@Valid @RequestBody TimetableEntryDTO.Request.CreateNewEntry entry) {
        try {
            schedulerService.createTimetableEntry(entry.getName(), entry.getGroup(), entry.getCronExpression(),
                    entry.getDescription(), entry.getDeviceId(), entry.getActionId(), entry.getArguments());
        } catch (SchedulerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Check job name");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/timetable-entries/group")
    public ResponseEntity<?> createTimetableEntryForGroup(@Valid @RequestBody TimetableEntryDTO.Request.CreateNewEntryForGroup entry) {
        try {
            schedulerService.createTimetableEntryForGroup(entry.getName(), entry.getGroup(), entry.getCronExpression(),
                    entry.getDescription(), entry.getActionId(), entry.getArguments());
        } catch (SchedulerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Check job name");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/timetable-entries/{id}")
    public ResponseEntity<?> updateTimetableEntry(@Valid @RequestBody TimetableEntryDTO.Request.UpdateEntry entry,
                                                  @PathVariable @Positive @NotNull Long id) {
        try {
            schedulerService.updateTimetableEntry(id, entry.getName(), entry.getCronExpression(), entry.getDescription(), entry.getArguments());
        } catch (SchedulerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Check job name");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/timetable-entries/{id}")
    public ResponseEntity<?> deleteTimetableEntry(@PathVariable @Positive @NotNull Long id) {
        try {
            boolean deleted = schedulerService.deleteTimetableEntry(id);
            if (deleted)
                return ResponseEntity.ok().build();
            else
                throw new ResponseStatusException(HttpStatus.CONFLICT);
        } catch (SchedulerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/timetable-entries/{id}/action")
    public ResponseEntity<?> makeActionWithTimetableEntry(@PathVariable @Positive @NotNull Long id,
                                                          @RequestParam("command") String command) {
        try {
            switch (command) {
                case "start" -> schedulerService.startJob(id);
                case "pause" -> schedulerService.pauseJob(id);
                case "resume" -> schedulerService.resumeJob(id);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } catch (SchedulerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
