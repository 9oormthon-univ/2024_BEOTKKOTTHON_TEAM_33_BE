package com.goormthon.rememberspring.diary.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners( value = { AuditingEntityListener.class } )
@Getter
public abstract class BaseTimeEntity {
    @CreatedDate
    @Column(name = "create_at")
    private LocalDateTime createAt;
}
