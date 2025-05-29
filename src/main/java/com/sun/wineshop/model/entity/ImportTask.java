package com.sun.wineshop.model.entity;

import com.sun.wineshop.model.enums.ImportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImportTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private ImportStatus status;

    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
