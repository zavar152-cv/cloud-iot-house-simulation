package ru.itmo.zavar.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.zavar.model.JobGroup;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "actions")
public class ActionEntity {
    @Id
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String action;

    @Positive
    @NotNull
    private Integer argumentsCount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private JobGroup actionGroup;
}
