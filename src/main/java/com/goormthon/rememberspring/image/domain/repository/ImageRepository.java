package com.goormthon.rememberspring.image.domain.repository;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.image.domain.Image;
import com.goormthon.rememberspring.member.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByMember(Member member);
    List<Image> findByDiaryAndMember(Diary diary, Member member);

}
