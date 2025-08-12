package com.chihuahua.sobok.member.spareTIme;

import com.chihuahua.sobok.member.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpareTimeRepository extends JpaRepository<SpareTime, Long> {

  List<SpareTime> findByMember(Member member);

  @Query("SELECT DISTINCT st FROM SpareTime st JOIN st.spareTimeDays std WHERE st.member = :member AND std.day IN :days")
  List<SpareTime> findByMemberAndDaysContaining(@Param("member") Member member,
      @Param("days") List<String> days);
}
