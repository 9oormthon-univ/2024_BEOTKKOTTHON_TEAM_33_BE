package com.goormthon.rememberspring.diary.domain.repository;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Optional<Diary> findByMember(Member member);
}
