package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;
import ru.itmo.zavar.model.JobGroup;
import ru.itmo.zavar.model.JobStatus;

import java.util.List;

public enum TimetableEntryDTO {
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
        @NotNull
        JobGroup getGroup();
    }

    private interface Status {
        @NotNull
        JobStatus getStatus();
    }

    private interface CronExpression {
        @NotBlank
        String getCronExpression();
    }

    private interface Description {
        @NotBlank
        String getDescription();
    }

    private interface DeviceId {
        @NotBlank
        String getDeviceId();
    }

    private interface DeviceName {
        @NotBlank
        String getDeviceName();
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

    private interface Arguments {
        @NotNull
        List<String> getArguments();
    }

    public enum Request {
        ;

        @Value
        public static class CreateNewEntry implements Name, Group, CronExpression, Description, DeviceId, ActionId, Arguments {
            String name;
            JobGroup group;
            String cronExpression;
            String description;
            String deviceId;
            Long actionId;
            List<String> arguments;
        }

        @Value
        public static class CreateNewEntryForGroup implements Name, Group, CronExpression, Description, ActionId, Arguments {
            String name;
            JobGroup group;
            String cronExpression;
            String description;
            Long actionId;
            List<String> arguments;
        }

        @Value
        public static class UpdateEntry implements Name, CronExpression, Description, ActionId, Arguments {
            String name;
            String cronExpression;
            String description;
            Long actionId;
            List<String> arguments;
        }
    }

    public enum Response {
        ;

        @Value
        public static class TimetableEntry implements Id, Group, Name, CronExpression, Description, DeviceId, ActionId, DeviceName, ActionName, Arguments {
            Long id;
            String name;
            JobGroup group;
            String cronExpression;
            String description;
            String deviceId;
            Long actionId;
            String deviceName;
            String actionName;
            List<String> arguments;
        }
    }

}
