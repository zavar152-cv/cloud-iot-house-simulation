package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

public enum GroupDTO {
    ;

    private interface Name {
        @NotBlank
        String getName();
    }

    private interface Status {
        @NotNull
        Boolean getStatus();
    }

    public enum Request {
        ;

    }

    public enum Response {
        ;
        @Value
        public static class GetGroupInfo implements Name, Status {
            String name;
            Boolean status;
        }
    }
}
