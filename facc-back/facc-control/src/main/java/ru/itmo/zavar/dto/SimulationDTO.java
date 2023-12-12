package ru.itmo.zavar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

public enum SimulationDTO {
    ;

    private interface StartCron {
        String getStartCronExpression();
    }

    private interface EndCron {
        String getEndCronExpression();
    }

    private interface StatusName {
        @NotBlank
        String getStatus();
    }

    public enum Request {
        ;
        @Value
        public static class SetSchedule implements StartCron, EndCron {
            String startCronExpression;
            String endCronExpression;
        }
    }

    public enum Response {
        ;
        @Value
        public static class GetSimulationInfo implements StartCron, EndCron, StatusName {
            String status;
            String startCronExpression;
            String endCronExpression;
        }
    }

}
