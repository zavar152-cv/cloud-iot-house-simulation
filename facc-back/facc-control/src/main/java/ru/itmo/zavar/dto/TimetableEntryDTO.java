package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;
import ru.itmo.zavar.util.JobGroup;
import ru.itmo.zavar.util.JobStatus;

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

    public enum Request {
        ;
        @Value
        public static class CreateNewEntry implements Name, Group, CronExpression, Description {
            String name;
            JobGroup group;
            String cronExpression;
            String description;
        }
        @Value
        public static class UpdateEntry implements Name, CronExpression, Description {
            String name;
            String cronExpression;
            String description;
        }
    }

    public enum Response {
        ;
        @Value
        public static class TimetableEntry implements Id, Group, Name, CronExpression, Description {
            Long id;
            String name;
            JobGroup group;
            String cronExpression;
            String description;
        }
    }

}
