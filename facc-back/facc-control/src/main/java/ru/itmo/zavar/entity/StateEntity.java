package ru.itmo.zavar.entity;

import jakarta.persistence.*;
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
    @OneToOne(fetch = FetchType.LAZY)
    private StatusEntity simulationStatus;
}
