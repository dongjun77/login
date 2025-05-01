package com.project.login.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.login.domain.Member;
import com.project.login.domain.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void memberRepositoryTest() {

        Member member = Member.builder()
                .email("edj@edj.com")
                .password(passwordEncoder.encode("123123"))
                .social(false)
                .nickname("edj")
                .build();
        member.addRole(MemberRole.USER);
        member.addRole(MemberRole.MANAGER);
        member.addRole(MemberRole.ADMIN);
        memberRepository.save(member);
    }

}