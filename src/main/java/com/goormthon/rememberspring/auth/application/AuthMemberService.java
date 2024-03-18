package com.goormthon.rememberspring.auth.application;

import com.goormthon.rememberspring.auth.api.dto.response.MemberLoginResDto;
import com.goormthon.rememberspring.auth.api.dto.response.UserInfo;
import com.goormthon.rememberspring.member.domain.Member;
import com.goormthon.rememberspring.member.domain.Role;
import com.goormthon.rememberspring.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthMemberService {

    private final MemberRepository memberRepository;

    public AuthMemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberLoginResDto saveUserInfo(UserInfo userInfo) {
        Member member = memberRepository.findByEmail(userInfo.email()).orElseGet(() -> createMember(userInfo));

        return MemberLoginResDto.from(member);
    }

    private Member createMember(UserInfo userInfo) {
        return memberRepository.save(
                Member.builder()
                        .email(userInfo.email())
                        .name(userInfo.nickname())
                        .picture(userInfo.picture())
                        .role(Role.ROLE_USER)
                        .build()
        );
    }

}
