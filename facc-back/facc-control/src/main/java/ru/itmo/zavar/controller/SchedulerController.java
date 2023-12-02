package ru.itmo.zavar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.zavar.dto.TimetableEntryDTO;
import ru.itmo.zavar.service.SchedulerService;

@RestController
@RequestMapping("/scheduler")
@RequiredArgsConstructor
@Slf4j(topic = "SchedulerController")
public class SchedulerController {
    private final SchedulerService schedulerService;

    @PostMapping("/timetable-entry")
    public ResponseEntity<?> createNewTimetableEntry(@Valid @RequestBody TimetableEntryDTO.Request.CreateNewEntry entry) {
        schedulerService.createNewTimetableEntry(entry.getName(), entry.getGroup(), entry.getCronExpression(), entry.getDescription());
        return ResponseEntity.ok().build();
    }

}
