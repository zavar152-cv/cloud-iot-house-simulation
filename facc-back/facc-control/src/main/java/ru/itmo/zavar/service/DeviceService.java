package ru.itmo.zavar.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ru.itmo.zavar.dto.DeviceDTO;
import ru.itmo.zavar.dto.GroupDTO;
import ru.itmo.zavar.dto.TypeDTO;
import ru.itmo.zavar.model.JobGroup;

import java.util.List;
import java.util.NoSuchElementException;

public interface DeviceService {
    void createDevice(String id, String name, JobGroup group, Long typeId) throws EntityNotFoundException, EntityExistsException;

    void updateDevice(String id, String name) throws NoSuchElementException;

    void changeDeviceStatus(String id, Boolean status) throws NoSuchElementException;

    void deleteDevice(String id) throws NoSuchElementException;

    List<DeviceDTO.Response.Device> getAllDevices();

    DeviceDTO.Response.Device getDeviceById(String id) throws NoSuchElementException;

    List<TypeDTO.Response.Type> getAllTypes();

    TypeDTO.Response.Type getTypeById(Long id) throws NoSuchElementException;

    List<GroupDTO.Response.GetGroupInfo> getAllGroups();

    void setGroupStatus(JobGroup jobGroup, boolean status);
}
