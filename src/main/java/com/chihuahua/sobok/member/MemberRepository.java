package com.chihuahua.sobok.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {

    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(String PhoneNumber);
    Boolean existsByUsername(String username);
    Boolean existsByDisplayName(String displayName);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);
}
