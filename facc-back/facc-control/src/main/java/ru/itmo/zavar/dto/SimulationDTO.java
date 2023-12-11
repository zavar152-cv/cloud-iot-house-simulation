package ru.itmo.zavar.dto;

import lombok.Value;

public enum SimulationDTO {
    ;

    private interface StartCron {
        String getStartCronExpression();
    }

    private interface EndCron {
        String getEndCronExpression();
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
        public static class GetSchedule implements StartCron, EndCron {
            String startCronExpression;
            String endCronExpression;
        }
    }

}
