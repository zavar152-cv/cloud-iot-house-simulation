package ru.itmo.zavar.entity;

import jakarta.persistence.Entity;
import lombok.*;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scheduler_job_info")
public class SchedulerJobInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;
    private String jobName;
    private String jobGroup;
    private String jobStatus;
    private String jobClass;
    private String cronExpression;
    private String description;
    private String interfaceName;
    private Long repeatTime;
    private Boolean cronJob;
}
