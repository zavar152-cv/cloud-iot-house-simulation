package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

public enum TypeDTO {
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

    public enum Request {
        ;

    }

    public enum Response {
        ;
        @Value
        public static class Type implements Id, Name {
            Long id;
            String name;
        }
    }

}
