package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

public enum ActionDTO {
    ;

    private interface Id {
        @NotNull
        @Positive
        Long getId();
    }

    private interface Name {
        @NotBlank
        String getName();
    }

    private interface Group {
        @NotBlank
        String getGroup();
    }

    private interface ArgumentsCount {
        @NotNull
        @Positive
        Integer getArgumentsCount();
    }

    public enum Request {
        ;

    }

    public enum Response {
        ;

        @Value
        public static class Action implements Id, Name, Group, ArgumentsCount {
            Long id;
            String name;
            String group;
            Integer argumentsCount;
        }
    }
}
