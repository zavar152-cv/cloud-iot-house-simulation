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
@Table(name = "commands_for_actions")
public class CommandForActionEntity {
    @Id
    private String command;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private ActionEntity action;
}
