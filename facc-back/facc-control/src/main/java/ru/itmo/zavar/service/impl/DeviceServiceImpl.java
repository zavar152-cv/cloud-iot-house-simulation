package ru.itmo.zavar.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.dto.DeviceDTO;
import ru.itmo.zavar.dto.GroupDTO;
import ru.itmo.zavar.entity.GroupOnEntity;
import ru.itmo.zavar.dto.TypeDTO;
import ru.itmo.zavar.entity.DeviceEntity;
import ru.itmo.zavar.entity.DeviceOnEntity;
import ru.itmo.zavar.entity.TypeEntity;
import ru.itmo.zavar.model.JobGroup;
import ru.itmo.zavar.mqtt.MqttSession;
import ru.itmo.zavar.repo.DeviceOnRepository;
import ru.itmo.zavar.repo.DeviceRepository;
import ru.itmo.zavar.repo.GroupOnRepository;
import ru.itmo.zavar.repo.TypeRepository;
import ru.itmo.zavar.service.CloudLoggingService;
import ru.itmo.zavar.service.DeviceService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "DeviceService")
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepository;
    private final TypeRepository typeRepository;
    private final DeviceOnRepository deviceOnRepository;
    private final GroupOnRepository groupOnRepository;
    private final CloudLoggingService cloudLoggingService;

    @Value("${yandex.mqtt.broker-url}")
    private String mqttBrokerUrl;

    @Value("${yandex.mqtt.registry-id}")
    private String mqttRegistryId;

    private final List<MqttSession> sessions = new ArrayList<>();

    @PostConstruct
    public void init() {
        Iterable<DeviceEntity> iterable = deviceRepository.findAll();
        iterable.forEach(deviceEntity -> {
            try {
                MqttSession mqttSession = new MqttSession(mqttBrokerUrl, getClass().getSimpleName() + ":" + deviceEntity.getId(), mqttRegistryId, cloudLoggingService);
                mqttSession.start();
                mqttSession.subscribe("$devices/" + deviceEntity.getId() + "/events");
                sessions.add(mqttSession);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PreDestroy
    public void destroy() {
        sessions.forEach(mqttSession -> {
            try {
                mqttSession.stop();
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void createDevice(String id, String name, JobGroup group, Long typeId) throws EntityNotFoundException, EntityExistsException {
        deviceRepository.findById(id).ifPresent(deviceEntity -> {
            throw new EntityExistsException("Device with this id exists");
        });

        TypeEntity typeEntity = typeRepository.findById(typeId).orElseThrow(() -> new EntityNotFoundException("Type not found"));
        DeviceEntity deviceEntity = DeviceEntity.builder()
                .id(id)
                .type(typeEntity)
                .status(false)
                .jobGroup(group)
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
        if (status) {
            if(deviceOnRepository.findByDevice_Id(deviceEntity.getId()).isEmpty())
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
    public List<GroupDTO.Response.GetGroupInfo> getAllGroups() {
        List<GroupDTO.Response.GetGroupInfo> all = new ArrayList<>();
        for (JobGroup jobGroup : JobGroup.values()) {
            boolean status = groupOnRepository.findByJobGroup(jobGroup).isPresent();
            all.add(new GroupDTO.Response.GetGroupInfo(jobGroup.name(), status));
        }
       return all;
    }

    @Override
    public void setGroupStatus(JobGroup jobGroup, boolean status) {
        Optional<GroupOnEntity> optionalGroupOn = groupOnRepository.findByJobGroup(jobGroup);
        if(status) {
            if(optionalGroupOn.isEmpty()) {
                groupOnRepository.save(GroupOnEntity.builder().jobGroup(jobGroup).build());
            }
        } else {
            optionalGroupOn.ifPresent(groupOnEntity -> groupOnRepository.deleteById(groupOnEntity.getId()));
        }
        List<DeviceEntity> all = deviceRepository.findAllByJobGroup(jobGroup);
        all.forEach(deviceEntity -> changeDeviceStatus(deviceEntity.getId(), status));
    }
}
