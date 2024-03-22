package com.goormthon.rememberspring.diary.domain.entity;

import com.goormthon.rememberspring.member.domain.Member;
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
public class DiaryLikeMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_like_member_id")
    private Long diaryLikeMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    private DiaryLikeMember(Diary diary, Member member) {
        this.diary = diary;
        this.member = member;
    }

    public static DiaryLikeMember toEntity(Diary diary, Member member) {
        return DiaryLikeMember.builder()
                .diary(diary)
                .member(member)
                .build();
    }
}
