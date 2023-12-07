package ru.itmo.zavar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.dto.CommandForActionDTO;
import ru.itmo.zavar.entity.CommandForActionEntity;
import ru.itmo.zavar.repo.CommandForActionRepository;
import ru.itmo.zavar.service.CommandForActionService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "CommandForActionService")
public class CommandForActionServiceImpl implements CommandForActionService {
    private final CommandForActionRepository commandForActionRepository;

    @Override
    public List<CommandForActionDTO.Response.CommandForAction> getAllCommandsForActions() {
        Iterable<CommandForActionEntity> iterable = commandForActionRepository.findAll();
        List<CommandForActionDTO.Response.CommandForAction> all = new ArrayList<>();
        iterable.forEach(entity -> all.add(new CommandForActionDTO.Response.CommandForAction(entity.getCommand(),
                entity.getAction().getId(), entity.getAction().getAction())));
        return all;
    }

    @Override
    public CommandForActionDTO.Response.CommandForAction getCommandForAction(String id) throws NoSuchElementException {
        CommandForActionEntity entity = commandForActionRepository.findById(id).orElseThrow();
        return new CommandForActionDTO.Response.CommandForAction(entity.getCommand(),
                entity.getAction().getId(), entity.getAction().getAction());
    }
}
