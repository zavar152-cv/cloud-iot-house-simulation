package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

public enum SimulationDTO {
    ;

    private interface StartCron {
        @NotBlank
        String getStartCronExpression();
    }

    private interface EndCron {
        @NotBlank
        String getEndCronExpression();
    }

    private interface StatusName {
        @NotBlank
        String getStatus();
    }

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
        @Value
        public static class AddSchedule implements StartCron, EndCron, Name {
            String name;
            String startCronExpression;
            String endCronExpression;
        }
        @Value
        public static class UpdateSchedule implements StartCron, EndCron {
            String startCronExpression;
            String endCronExpression;
        }
    }

    public enum Response {
        ;
        @Value
        public static class GetSimulationInfo implements StatusName {
            String status;
        }

        @Value
        public static class GetSimulationSchedule implements StartCron, EndCron, Id, Name {
            Long id;
            String name;
            String startCronExpression;
            String endCronExpression;
        }
    }

}
