package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

public enum CommandForActionDTO {
    ;

    private interface Id {
        @NotNull
        @Positive
        Long getId();
    }

    private interface FileName {
        String getFileName();
    }

    private interface FileId {
        @Positive
        Long getFileId();
    }

    private interface Command {
        @NotBlank
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
        public static class CommandForAction implements Id, Command, ActionId, ActionName, FileId, FileName {
            Long id;
            String command;
            Long actionId;
            String actionName;
            Long fileId;
            String fileName;
        }
    }

}
