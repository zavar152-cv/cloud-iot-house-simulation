package ru.itmo.zavar.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.zavar.converter.StringListConverter;
import ru.itmo.zavar.model.JobGroup;
import ru.itmo.zavar.model.JobStatus;

import java.util.List;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private DeviceEntity device;
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private ActionEntity action;
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
    @Convert(converter = StringListConverter.class)
    @Column(name = "arguments", nullable = false)
    private List<String> arguments;
}
