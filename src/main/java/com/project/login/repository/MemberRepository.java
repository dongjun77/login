package com.project.login.repository;

import com.project.login.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @EntityGraph(attributePaths = "memberRoleList")
    Optional<Member> findByEmail(String email);

}
