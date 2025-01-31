package com.apple.sobok.survey;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Optional<Survey> findByMember(Member member);
}
