package ru.itmo.zavar.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ru.itmo.zavar.dto.DeviceDTO;
import ru.itmo.zavar.dto.TypeDTO;

import java.util.List;
import java.util.NoSuchElementException;

public interface DeviceService {
    void createDevice(String id, String name, Long typeId) throws EntityNotFoundException, EntityExistsException;

    void updateDevice(String id, String name) throws NoSuchElementException;

    void deleteDevice(String id) throws NoSuchElementException;

    List<DeviceDTO.Response.Device> getAllDevices();

    DeviceDTO.Response.Device getDeviceById(String id) throws NoSuchElementException;

    List<TypeDTO.Response.Type> getAllTypes();

    TypeDTO.Response.Type getTypeById(Long id) throws NoSuchElementException;
}
