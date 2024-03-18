package com.goormthon.rememberspring.diary.domain.entity;

import com.goormthon.rememberspring.image.domain.Image;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

}
