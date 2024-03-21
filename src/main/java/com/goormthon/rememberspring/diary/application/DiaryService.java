package com.goormthon.rememberspring.diary.application;

import com.goormthon.rememberspring.diary.api.dto.response.DiaryResDto;
import com.goormthon.rememberspring.diary.api.dto.response.HashtagDiariesResDto;
import com.goormthon.rememberspring.diary.domain.entity.DiaryHashtagMapping;
import com.goormthon.rememberspring.diary.domain.repository.DiaryHashtagRepository;
import com.goormthon.rememberspring.diary.domain.repository.DiaryRepository;
import com.goormthon.rememberspring.member.domain.Member;
import com.goormthon.rememberspring.member.domain.repository.MemberRepository;
import com.goormthon.rememberspring.member.exception.MemberNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final DiaryHashtagRepository diaryHashtagRepository;

    // 모아서 보기
    public List<HashtagDiariesResDto> gatherAllDiaries(String email, int page, int size) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        List<HashtagDiariesResDto> hashtagDiariesResDtoList = new ArrayList<>();

        // 월에 대한 유저의 일기
        hashtagDiariesResDtoList.add(HashtagDiariesResDto.of(
                String.format("%d월의 일기", getMonth()),
                getMonthDiaries(member, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "diaryId"))))
        );

        // 멤버가 가지고 있는 다이어리에서 유니크 해시태그 가져오기.
        List<DiaryHashtagMapping> memberDiaryHashtagMapping = diaryHashtagRepository.findAllByMemberDiaryHashtag(member);
        List<String> uniqueHashtags = getUniqueHashtags(memberDiaryHashtagMapping);

        for (String hashtagName : uniqueHashtags) {
            // 멤버가 가지고 있는 다이어리와 매핑된 해시태그를 가져온다.
            Page<DiaryHashtagMapping> diaryHashtagMappings = diaryHashtagRepository.findByDiary_MemberAndHashtag_Name(
                    member,
                    hashtagName,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
            );
            Page<DiaryResDto> diaryResDtos = getDiaryResDtos(diaryHashtagMappings);

            hashtagDiariesResDtoList.add(HashtagDiariesResDto.of(hashtagName, diaryResDtos));
        }

        return hashtagDiariesResDtoList;
    }

    // 월간 다이어리
    private Page<DiaryResDto> getMonthDiaries(Member member, Pageable pageable) {
        return diaryRepository.findByMonth(member, getMonth(), pageable);
    }

    private int getMonth() {
        return LocalDate.now().getMonth().getValue();
    }

    // 나의 다이어리와 연결되어 있는 해시태그 이름을 가져온다.
    private List<String> getUniqueHashtags(List<DiaryHashtagMapping> memberDiaryHashtagMappings) {
        return memberDiaryHashtagMappings.stream()
                .map(mapping -> mapping.getHashtag().getName())
                .distinct()
                .collect(Collectors.toList());
    }

    private Page<DiaryResDto> getDiaryResDtos(Page<DiaryHashtagMapping> diaryHashtagMappings) {
        List<DiaryResDto> content = diaryHashtagMappings.getContent().stream()
                .map(DiaryHashtagMapping::getDiary)
                .map(DiaryResDto::from)
                .collect(Collectors.toList());

        return new PageImpl<>(content, diaryHashtagMappings.getPageable(), diaryHashtagMappings.getTotalElements());
    }

    // 순서대로 보기


    // 함께보기

}
