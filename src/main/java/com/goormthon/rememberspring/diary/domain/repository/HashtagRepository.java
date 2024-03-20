package com.goormthon.rememberspring.diary.domain.repository;

import com.goormthon.rememberspring.diary.domain.entity.Hashtag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByName(String tagName);
}
