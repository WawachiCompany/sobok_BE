package com.chihuahua.sobok.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MemberLinkAppRepository extends JpaRepository<MemberLinkApp, Long> {

  @Modifying
  @Query("DELETE FROM MemberLinkApp m WHERE m.member.id = :memberId")
  void deleteByMemberId(@Param("memberId") Long memberId);
}
