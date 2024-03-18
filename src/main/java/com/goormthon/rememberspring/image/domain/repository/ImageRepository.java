package com.goormthon.rememberspring.image.domain.repository;

import com.goormthon.rememberspring.image.domain.Image;
import com.goormthon.rememberspring.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByMember(Member mEmber);
}
