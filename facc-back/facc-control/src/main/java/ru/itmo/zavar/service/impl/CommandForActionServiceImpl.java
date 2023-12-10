package ru.itmo.zavar.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.zavar.dto.CommandForActionDTO;
import ru.itmo.zavar.entity.CommandForActionEntity;
import ru.itmo.zavar.entity.FileEntity;
import ru.itmo.zavar.exception.StorageException;
import ru.itmo.zavar.repo.CommandForActionRepository;
import ru.itmo.zavar.repo.FileRepository;
import ru.itmo.zavar.service.CommandForActionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "CommandForActionService")
public class CommandForActionServiceImpl implements CommandForActionService {
    private final CommandForActionRepository commandForActionRepository;
    private final FileRepository fileRepository;

    @Override
    public List<CommandForActionDTO.Response.CommandForAction> getAllCommandsForActions() {
        Iterable<CommandForActionEntity> iterable = commandForActionRepository.findAll();
        List<CommandForActionDTO.Response.CommandForAction> all = new ArrayList<>();
        iterable.forEach(entity -> all.add(new CommandForActionDTO.Response.CommandForAction(entity.getCommand(),
                entity.getAction().getId(), entity.getAction().getAction())));
        return all;
    }

    @Override
    public CommandForActionDTO.Response.CommandForAction getCommandForAction(Long id) throws NoSuchElementException {
        CommandForActionEntity entity = commandForActionRepository.findById(id).orElseThrow();
        return new CommandForActionDTO.Response.CommandForAction(entity.getCommand(),
                entity.getAction().getId(), entity.getAction().getAction());
    }

    @Override
    public void attachFile(Long id, MultipartFile file) throws NoSuchElementException, StorageException, EntityExistsException {
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file");
        }
        CommandForActionEntity commandForActionEntity = commandForActionRepository.findById(id).orElseThrow();
        if(commandForActionEntity.getFile() != null) {
            throw new EntityExistsException("File was attached later, delete it first");
        }
        try {
            String filename = file.getOriginalFilename();
            byte[] bytes = file.getBytes();

            FileEntity fileEntity = FileEntity.builder()
                    .name(filename)
                    .content(bytes).build();

            fileRepository.save(fileEntity);
            commandForActionEntity.setFile(fileEntity);
            commandForActionRepository.save(commandForActionEntity);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    @Override
    public void detachFile(Long id) throws NoSuchElementException, EntityNotFoundException {
        CommandForActionEntity commandForActionEntity = commandForActionRepository.findById(id).orElseThrow();
        FileEntity file = commandForActionEntity.getFile();
        if(file != null) {
            commandForActionEntity.setFile(null);
            commandForActionRepository.save(commandForActionEntity);
            fileRepository.delete(file);
        } else {
            throw new EntityNotFoundException("File wasn't attached");
        }
    }
}
