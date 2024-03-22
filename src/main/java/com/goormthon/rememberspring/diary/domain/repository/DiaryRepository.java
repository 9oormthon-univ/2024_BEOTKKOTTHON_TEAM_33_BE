package com.goormthon.rememberspring.diary.domain.repository;

import com.goormthon.rememberspring.diary.api.dto.response.DiaryResDto;
import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("SELECT d FROM Diary d WHERE d.member = :member AND MONTH(d.createAt) = :month")
    Page<DiaryResDto> findByMonth(Member member, int month, Pageable pageable);

    @Query("SELECT d FROM Diary d WHERE d.member = :member ")
    Page<DiaryResDto> findByMember(Member member, Pageable pageable);

    @Query("select d FROM Diary d WHERE d.isPublic = true AND YEAR(d.createAt) = :year AND MONTH(d.createAt) = :month")
    Page<Diary> findAllByIsPublicAndYearAndMonth(int year, int month, Pageable pageable);

}