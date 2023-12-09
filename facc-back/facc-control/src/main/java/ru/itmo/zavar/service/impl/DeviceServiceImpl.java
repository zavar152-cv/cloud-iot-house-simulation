package ru.itmo.zavar.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.dto.DeviceDTO;
import ru.itmo.zavar.dto.TypeDTO;
import ru.itmo.zavar.entity.DeviceEntity;
import ru.itmo.zavar.entity.DeviceOnEntity;
import ru.itmo.zavar.entity.TypeEntity;
import ru.itmo.zavar.model.JobGroup;
import ru.itmo.zavar.repo.DeviceOnRepository;
import ru.itmo.zavar.repo.DeviceRepository;
import ru.itmo.zavar.repo.TypeRepository;
import ru.itmo.zavar.service.DeviceService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "DeviceService")
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepository;
    private final TypeRepository typeRepository;
    private final DeviceOnRepository deviceOnRepository;

    @Override
    public void createDevice(String id, String name, Long typeId) throws EntityNotFoundException, EntityExistsException {
        deviceRepository.findById(id).ifPresent(deviceEntity -> {
            throw new EntityExistsException("Device with this id exists");
        });

        TypeEntity typeEntity = typeRepository.findById(typeId).orElseThrow(() -> new EntityNotFoundException("Type not found"));
        DeviceEntity deviceEntity = DeviceEntity.builder()
                .id(id)
                .type(typeEntity)
                .status(false)
                .name(name).build();
        deviceRepository.save(deviceEntity);
    }

    @Override
    public void updateDevice(String id, String name) throws NoSuchElementException {
        DeviceEntity deviceEntity = deviceRepository.findById(id).orElseThrow();
        deviceEntity.setName(name);
        deviceRepository.save(deviceEntity);
    }

    @Override
    public void changeDeviceStatus(String id, Boolean status) throws NoSuchElementException {
        DeviceEntity deviceEntity = deviceRepository.findById(id).orElseThrow();
        deviceEntity.setStatus(status);
        deviceRepository.save(deviceEntity);
        if(status) {
            deviceOnRepository.save(DeviceOnEntity.builder().device(deviceEntity).build());
        } else {
            deviceOnRepository.deleteByDevice_Id(deviceEntity.getId());
        }
    }

    @Override
    public void deleteDevice(String id) throws NoSuchElementException {
        deviceRepository.deleteById(id);
    }

    @Override
    public List<DeviceDTO.Response.Device> getAllDevices() {
        Iterable<DeviceEntity> iterable = deviceRepository.findAll();
        List<DeviceDTO.Response.Device> all = new ArrayList<>();
        iterable.forEach(deviceEntity -> all.add(new DeviceDTO.Response.Device(deviceEntity.getId(),
                deviceEntity.getName(), deviceEntity.getType().getName(), deviceEntity.getStatus())));
        return all;
    }

    @Override
    public DeviceDTO.Response.Device getDeviceById(String id) throws NoSuchElementException {
        DeviceEntity deviceEntity = deviceRepository.findById(id).orElseThrow();
        return new DeviceDTO.Response.Device(deviceEntity.getId(),
                deviceEntity.getName(), deviceEntity.getType().getName(), deviceEntity.getStatus());
    }

    @Override
    public List<TypeDTO.Response.Type> getAllTypes() {
        Iterable<TypeEntity> iterable = typeRepository.findAll();
        List<TypeDTO.Response.Type> all = new ArrayList<>();
        iterable.forEach(typeEntity -> all.add(new TypeDTO.Response.Type(typeEntity.getId(), typeEntity.getName())));
        return all;
    }

    @Override
    public TypeDTO.Response.Type getTypeById(Long id) throws NoSuchElementException {
        TypeEntity typeEntity = typeRepository.findById(id).orElseThrow();
        return new TypeDTO.Response.Type(typeEntity.getId(), typeEntity.getName());
    }

    @Override
    public List<String> getAllGroups() {
        return Stream.of(JobGroup.values()).map(Enum::name).toList();
    }
}
