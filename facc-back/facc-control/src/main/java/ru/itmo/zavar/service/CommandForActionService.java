package ru.itmo.zavar.service;

import ru.itmo.zavar.dto.CommandForActionDTO;

import java.util.List;
import java.util.NoSuchElementException;

public interface CommandForActionService {
    List<CommandForActionDTO.Response.CommandForAction> getAllCommandsForActions();

    CommandForActionDTO.Response.CommandForAction getCommandForAction(String id) throws NoSuchElementException;
}
