package ru.itmo.zavar.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.itmo.zavar.util.JobGroup;
import ru.itmo.zavar.util.JobStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetable")
public class TimetableEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @Enumerated(EnumType.STRING)
    @NotNull
    private JobGroup jobGroup;
    @Enumerated(EnumType.STRING)
    @NotNull
    private JobStatus jobStatus;
    @NotBlank
    private String className;
    @NotBlank
    private String cronExpression;
    @NotBlank
    private String description;
}
