package com.goormthon.rememberspring.diary.domain.repository;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.diary.domain.entity.DiaryHashtagMapping;
import com.goormthon.rememberspring.diary.domain.entity.Hashtag;
import com.goormthon.rememberspring.member.domain.Member;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DiaryHashtagRepository extends JpaRepository<DiaryHashtagMapping, Long> {
    boolean existsByDiaryAndHashtag(Diary diary, Hashtag hashtag);

    @Query("select d "
            + "from DiaryHashtagMapping d "
            + "where d.diary.member = :member")
    List<DiaryHashtagMapping> findAllByMemberDiaryHashtag(Member member);

    Page<DiaryHashtagMapping> findByDiary_MemberAndHashtag_Name(Member member, String hashtagName, Pageable pageable);
}
