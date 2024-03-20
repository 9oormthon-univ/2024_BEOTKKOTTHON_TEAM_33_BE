package com.goormthon.rememberspring.diary.domain.repository;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Optional<Diary> findByMember(Member member);

    Page<Diary> findByMember(Member member, Pageable pageable);

    List<Diary> findAllByMember(Member member);

    @Query("SELECT d FROM Diary d WHERE d.member = :member AND YEAR(d.createAt) = :year AND MONTH(d.createAt) = :month")
    List<Diary> findByYearAndMonth(Member member, int year, int month);


}