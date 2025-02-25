package com.apple.sobok.routine.todo;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByMemberAndCategory(Member member, String category);

    List<Category> findByMemberAndCreatedAtBetween(Member member, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);
}
