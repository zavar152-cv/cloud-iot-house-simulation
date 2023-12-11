package ru.itmo.zavar.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "state")
public class StateEntity {
    @Id
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    private StatusEntity simulationStatus;

    private String startCronExpression;

    private String endCronExpression;
}
