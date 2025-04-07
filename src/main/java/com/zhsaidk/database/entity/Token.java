package com.zhsaidk.database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "user_id")
    private Integer userId;  // Связь с пользователем

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}