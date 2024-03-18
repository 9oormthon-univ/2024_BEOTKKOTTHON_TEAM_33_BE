package com.goormthon.rememberspring.diary.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Diary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long diaryId;

    // 일기 제목
    @Column(nullable = false)
    private String title;

    // ManyToOne 으로 User 객체 변경 예정
    @Column(nullable = false)
    private String writer;

    // 일기 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaryType diaryType;

    // 감정 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Emotion emotion;

    @Column(nullable = false)
    private String content;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;
}
