package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

public enum CommandForActionDTO {
    ;

    private interface Command {
        @NotNull
        String getCommand();
    }

    private interface ActionId {
        @NotNull
        @Positive
        Long getActionId();
    }

    private interface ActionName {
        @NotBlank
        String getActionName();
    }

    public enum Request {
        ;

    }

    public enum Response {
        ;

        @Value
        public static class CommandForAction implements Command, ActionId, ActionName {
            String command;
            Long actionId;
            String actionName;
        }
    }

}
