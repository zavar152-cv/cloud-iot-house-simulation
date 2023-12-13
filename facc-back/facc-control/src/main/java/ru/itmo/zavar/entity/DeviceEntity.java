package ru.itmo.zavar.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "devices")
public class DeviceEntity {
    @Id
    private String id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    private TypeEntity type;

    @Enumerated(EnumType.STRING)
    @NotNull
    private JobGroup jobGroup;

    @NotNull
    private Boolean status;
}
