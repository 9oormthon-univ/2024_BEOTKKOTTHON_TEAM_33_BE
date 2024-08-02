package com.goormthon.rememberspring.diary.application;

import com.goormthon.rememberspring.diary.api.dto.response.DiaryResDto;
import com.goormthon.rememberspring.diary.api.dto.response.HashtagDiariesResDto;
import com.goormthon.rememberspring.diary.api.dto.response.PublicDiaryResDto;
import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.diary.domain.entity.DiaryHashtagMapping;
import com.goormthon.rememberspring.diary.domain.entity.DiaryLikeMember;
import com.goormthon.rememberspring.diary.domain.repository.DiaryHashtagRepository;
import com.goormthon.rememberspring.diary.domain.repository.DiaryLikeMemberRepository;
import com.goormthon.rememberspring.diary.domain.repository.DiaryRepository;
import com.goormthon.rememberspring.diary.excepion.DiaryAccessDeniedException;
import com.goormthon.rememberspring.diary.excepion.DiaryNotFoundException;
import com.goormthon.rememberspring.diary.excepion.ExistsLikeDiaryException;
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
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final DiaryHashtagRepository diaryHashtagRepository;
    private final DiaryLikeMemberRepository diaryLikeMemberRepository;

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

    // 순서대로보기
    public List<HashtagDiariesResDto> orderAllDiaries(String email, int page, int size) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

        Page<DiaryResDto> diaries = diaryRepository.findByMember(
                member,
                PageRequest.of(page, size, Sort.by(Direction.DESC, "diaryId"))
        );

        return List.of(HashtagDiariesResDto.from(diaries));
    }

    // 다이어리 상세보기
    public DiaryResDto getDiary(String email, Long diaryId) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

        boolean isLike = diaryLikeMemberRepository.existsByDiaryAndMember(diary, member);
        int likeCount = diaryLikeMemberRepository.countByDiary(diary);

        if (!diary.getMember().getMemberId().equals(member.getMemberId()) && !diary.isPublic()) {
            throw new DiaryAccessDeniedException("공유되지 않은 다이어리입니다!");
        }

        return DiaryResDto.of(diary, isLike, likeCount);
    }

    // 다이어리 공유하기, 공유취소
    @Transactional
    public boolean updatePublic(String email, Long diaryId) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

        // 다이어리를 공유할 떄, 작성자 본인이 아니면 조작이 불가능하다. 예외처리
        if (!diary.getMember().getMemberId().equals(member.getMemberId())) {
            throw new DiaryAccessDeniedException();
        }

        diary.updateIsPublic();

        return diary.isPublic();
    }

    // 함께보기
    public Page<PublicDiaryResDto> publicAllDiaries(String email, String sortProperty, int year, int month, int page, int size) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Page<Diary> diaries = diaryRepository.findAllByIsPublicAndYearAndMonth(
                year,
                month,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortProperty))
        );

        return diaries.map(diary -> mapToPublicDiary(diary, member));
    }

    private PublicDiaryResDto mapToPublicDiary(Diary diary, Member member) {
        boolean isLike = diaryLikeMemberRepository.existsByDiaryAndMember(diary, member);
        int likeCount = diaryLikeMemberRepository.countByDiary(diary);

        return PublicDiaryResDto.of(diary, isLike, likeCount);
    }

    // 함께보기 다이어리 상세보기
    public PublicDiaryResDto getPublicDiary(String email, Long diaryId) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

        boolean isLike = diaryLikeMemberRepository.existsByDiaryAndMember(diary, member);
        int likeCount = diaryLikeMemberRepository.countByDiary(diary);

        if (!diary.isPublic()) {
            throw new DiaryAccessDeniedException("공유되지 않은 다이어리입니다!");
        }

        return PublicDiaryResDto.of(diary, isLike, likeCount);
    }

    // 다이어리 좋아요
    @Transactional
    public void likeDiary(String email, Long diaryId) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

        if (diaryLikeMemberRepository.existsByDiaryAndMember(diary, member)) {
            throw new ExistsLikeDiaryException("이미 좋아요를 눌렀습니다.");
        }

        diary.updateLikeCount();
        diaryLikeMemberRepository.save(DiaryLikeMember.toEntity(diary, member));
    }

    // 다이어리 취소
    @Transactional
    public void cancelLikeDiary(String email, Long diaryId) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

        if (!diaryLikeMemberRepository.existsByDiaryAndMember(diary, member)) {
            throw new ExistsLikeDiaryException("존재하는 좋아요가 없습니다.");
        }

        DiaryLikeMember diaryLikeMember = diaryLikeMemberRepository.findByDiaryAndMember(diary, member).orElseThrow();

        diary.updateCancelLikeCount();
        diaryLikeMemberRepository.delete(diaryLikeMember);
    }

    // 다이어리 수정


    // 다이어리 삭제

}
