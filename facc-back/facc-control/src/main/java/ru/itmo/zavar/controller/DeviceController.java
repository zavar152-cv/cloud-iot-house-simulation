package ru.itmo.zavar.controller;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.dto.ActionDTO;
import ru.itmo.zavar.dto.CommandForActionDTO;
import ru.itmo.zavar.dto.DeviceDTO;
import ru.itmo.zavar.service.ActionService;
import ru.itmo.zavar.service.CommandForActionService;
import ru.itmo.zavar.service.DeviceService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
@Slf4j(topic = "DeviceController")
public class DeviceController {
    private final DeviceService deviceService;
    private final ActionService actionService;
    private final CommandForActionService commandForActionService;

    @GetMapping("/actions")
    public ResponseEntity<List<ActionDTO.Response.Action>> getAllActions() {
        var all = actionService.getAllActions();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/actions/{id}")
    public ResponseEntity<ActionDTO.Response.Action> getActionById(@PathVariable @Positive @NotNull Long id) {
        try {
            var action = actionService.getActionById(id);
            return ResponseEntity.ok(action);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/commands-for-actions")
    public ResponseEntity<List<CommandForActionDTO.Response.CommandForAction>> getAllCommandsForActions() {
        var all = commandForActionService.getAllCommandsForActions();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/commands-for-actions/{id}")
    public ResponseEntity<CommandForActionDTO.Response.CommandForAction> getCommandForActionById(@PathVariable @Positive @NotNull String id) {
        try {
            var action = commandForActionService.getCommandForAction(id);
            return ResponseEntity.ok(action);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/devices")
    public ResponseEntity<List<DeviceDTO.Response.Device>> getAllDevices() {
        var all = deviceService.getAllDevices();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<DeviceDTO.Response.Device> getDeviceById(@PathVariable @Positive @NotNull String id) {
        try {
            var device = deviceService.getDeviceById(id);
            return ResponseEntity.ok(device);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/devices")
    public ResponseEntity<?> createDevice(@Valid @RequestBody DeviceDTO.Request.CreateNewDevice createNewDevice) {
        try {
            deviceService.createDevice(createNewDevice.getId(), createNewDevice.getName(), createNewDevice.getTypeId());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (EntityExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Device with this name exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/devices/{id}")
    public ResponseEntity<?> updateDevice(@Valid @RequestBody DeviceDTO.Request.UpdateDevice updateDevice,
                                          @PathVariable @Positive @NotNull String id) {
        try {
            deviceService.updateDevice(id, updateDevice.getName());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable @Positive @NotNull String id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }
}
