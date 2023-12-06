package ru.itmo.zavar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.dto.ActionDTO;
import ru.itmo.zavar.entity.ActionEntity;
import ru.itmo.zavar.repository.ActionRepository;
import ru.itmo.zavar.service.ActionService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "ActionService")
public class ActionServiceImpl implements ActionService {
    private final ActionRepository actionRepository;

    @Override
    public List<ActionDTO.Response.Action> getAllActions() {
        Iterable<ActionEntity> iterable = actionRepository.findAll();
        List<ActionDTO.Response.Action> all = new ArrayList<>();
        iterable.forEach(actionEntity -> all.add(new ActionDTO.Response.Action(actionEntity.getId(),
                actionEntity.getAction(), actionEntity.getActionGroup().name(), actionEntity.getArgumentsCount())));
        return all;
    }

    @Override
    public ActionDTO.Response.Action getActionById(Long id) throws NoSuchElementException {
        ActionEntity actionEntity = actionRepository.findById(id).orElseThrow();
        return new ActionDTO.Response.Action(actionEntity.getId(),
                actionEntity.getAction(), actionEntity.getActionGroup().name(), actionEntity.getArgumentsCount());
    }
}
