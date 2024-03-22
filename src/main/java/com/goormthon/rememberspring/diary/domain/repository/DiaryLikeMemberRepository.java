package com.goormthon.rememberspring.diary.domain.repository;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.diary.domain.entity.DiaryLikeMember;
import com.goormthon.rememberspring.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryLikeMemberRepository extends JpaRepository<DiaryLikeMember, Long> {
    boolean existsByDiaryAndMember(Diary diary, Member member);

    int countByDiary(Diary diary);

    Optional<DiaryLikeMember> findByDiaryAndMember(Diary diary, Member member);
}
