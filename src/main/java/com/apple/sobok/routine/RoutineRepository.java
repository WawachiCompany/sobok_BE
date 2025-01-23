package com.apple.sobok.routine;


import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    @Query(value = "SELECT r.* " +
            "FROM routine r " +
            "JOIN routine_days rd ON r.id = rd.routine_id " +
            "WHERE r.user_id = :userId AND rd.day = :day AND r.is_suspended = false AND r.is_ended = false", nativeQuery = true)
    List<Routine> findByUserIdAndDay(@Param("userId") Long id, @Param("day") String day);

    @Query(value = "SELECT r.* " +
            "FROM routine r " +
            "JOIN routine_days rd ON r.id = rd.routine_id " +
            "WHERE r.user_id = :userId AND rd.day = :day AND r.is_suspended = false AND r.is_ended = false And r.is_completed = true", nativeQuery = true)
    List<Routine> findByUserIdAndDayCompleted(@Param("userId") Long id, @Param("day") String day);

    List<Routine> findByMember(Member member);

    Optional<Routine> findByMemberAndId(Member member, Long id);

    List<Routine> findByMemberAndIsEnded(Member member, Boolean isEnded);
}
