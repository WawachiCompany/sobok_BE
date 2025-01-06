package com.apple.sobok.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {

    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(String PhoneNumber);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);
}
