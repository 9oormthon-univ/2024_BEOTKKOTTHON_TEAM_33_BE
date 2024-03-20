package com.goormthon.rememberspring.diary.domain.repository;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.diary.domain.entity.DiaryHashtagMapping;
import com.goormthon.rememberspring.diary.domain.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryHashtagRepository extends JpaRepository<DiaryHashtagMapping, Long> {
    boolean existsByDiaryAndHashtag(Diary diary, Hashtag hashtag);
}
