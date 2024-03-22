package com.goormthon.rememberspring.image.domain;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.member.domain.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    private String convertImageUrl;

    private int imageSequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Builder
    private Image(String convertImageUrl, int imageSequence, Member member, Diary diary) {
        this.convertImageUrl = convertImageUrl;
        this.imageSequence = imageSequence;
        this.member = member;
        this.diary = diary;
    }

    // 확인버튼 눌렀을 때, 이 로직 사용해서 다이어리 값 넣어주면 됩니다.
    public void updateImage(Diary diary) {
        this.diary = diary;
    }

}
