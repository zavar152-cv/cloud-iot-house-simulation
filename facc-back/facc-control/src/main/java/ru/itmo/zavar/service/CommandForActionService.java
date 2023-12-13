package ru.itmo.zavar.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.zavar.dto.CommandForActionDTO;
import ru.itmo.zavar.exception.StorageException;

import java.util.List;
import java.util.NoSuchElementException;

public interface CommandForActionService {
    List<CommandForActionDTO.Response.CommandForAction> getAllCommandsForActions();

    CommandForActionDTO.Response.CommandForAction getCommandForAction(Long id) throws NoSuchElementException;

    void attachFile(Long id, MultipartFile file) throws NoSuchElementException, StorageException, EntityExistsException;

    void detachFile(Long id) throws NoSuchElementException, EntityNotFoundException;
}
