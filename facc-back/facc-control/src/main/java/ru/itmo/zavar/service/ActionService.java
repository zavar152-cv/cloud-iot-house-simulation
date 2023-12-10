package ru.itmo.zavar.service;

import ru.itmo.zavar.dto.ActionDTO;

import java.util.List;
import java.util.NoSuchElementException;

public interface ActionService {
    List<ActionDTO.Response.Action> getAllActions();

    ActionDTO.Response.Action getActionById(Long id) throws NoSuchElementException;
}
