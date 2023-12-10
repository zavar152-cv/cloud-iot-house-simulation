package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;
import lombok.Value;

public enum DeviceDTO {
    ;

    private interface Id {
        @NotBlank
        String getId();
    }

    private interface Name {
        @NotBlank
        String getName();
    }

    private interface Type {
        @NotBlank
        String getType();
    }

    private interface TypeId {
        @NotNull
        @Positive
        Long getTypeId();
    }

    private interface Status {
        @NotNull
        Boolean getStatus();
    }

    public enum Request {
        ;

        @Value
        public static class CreateNewDevice implements Id, Name, TypeId {
            String id;
            String name;
            Long typeId;
        }

        @Value
        @NoArgsConstructor(force = true)
        public static class UpdateDevice implements Name {
            String name;
        }

        @Value
        @NoArgsConstructor(force = true)
        public static class ChangeDeviceStatus implements Status {
            Boolean status;
        }
    }

    public enum Response {
        ;

        @Value
        public static class Device implements Id, Name, Type, Status {
            String id;
            String name;
            String type;
            Boolean status;
        }
    }
}
