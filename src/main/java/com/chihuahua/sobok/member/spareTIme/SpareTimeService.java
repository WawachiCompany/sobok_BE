package com.chihuahua.sobok.member.spareTIme;

import com.chihuahua.sobok.member.Member;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpareTimeService {

  private final SpareTimeRepository spareTimeRepository;

  public Map<String, Object> getSpareTimeByDate(Member member, String day) {
    List<SpareTime> spareTimeList = spareTimeRepository.findByMemberAndDaysContaining(member,
        List.of(day));
    var result = spareTimeList.stream()
        .map(spareTime -> {
          SpareTimeResponseDto dto = new SpareTimeResponseDto();
          dto.setId(spareTime.getId());
          dto.setTitle(spareTime.getTitle());
          dto.setStartTime(spareTime.getStartTime());
          dto.setEndTime(spareTime.getEndTime());
          dto.setDuration(spareTime.getDuration());
          dto.setDays(spareTime.getDays());
          return dto;
        })
        .toList();

    long totalDuration = result.stream()
        .mapToLong(SpareTimeResponseDto::getDuration)
        .sum();

    Map<String, Object> map = new HashMap<>();
    map.put("duration", totalDuration);
    map.put("spareTimeList", result);
    return map;
  }

  public Map<String, Long> getSpareTimeDuration(Member member) {
    List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY",
        "SUNDAY");
    Map<String, Long> result = new HashMap<>();
    days.forEach(day -> {
      List<SpareTime> spareTimeList = spareTimeRepository.findByMemberAndDaysContaining(member,
          List.of(day));
      if (spareTimeList.isEmpty()) {
        result.put(day.toLowerCase(), 0L);
      } else {
        long totalDuration = spareTimeList.stream()
            .mapToLong(SpareTime::getDuration)
            .sum();
        result.put(day.toLowerCase(), totalDuration);
      }
    });
    return result;
  }

  public void save(Member member, SpareTimeDto spareTimeDto) {

    LocalTime startTime = LocalTime.parse(spareTimeDto.getStartTime());
    LocalTime endTime = LocalTime.parse(spareTimeDto.getEndTime());
    SpareTime spareTime = new SpareTime();
    spareTime.setTitle(spareTimeDto.getTitle());
    spareTime.setStartTime(spareTimeDto.getStartTime());
    spareTime.setEndTime(spareTimeDto.getEndTime());
    spareTime.setMember(member);
    spareTime.setDuration(Duration.between(startTime, endTime).toMinutes());
    spareTime.setDays(spareTimeDto.getDays()); // 헬퍼 메서드 사용
    spareTimeRepository.save(spareTime);
  }

  public void update(SpareTimeDto spareTimeDto) {
    LocalTime startTime = LocalTime.parse(spareTimeDto.getStartTime());
    LocalTime endTime = LocalTime.parse(spareTimeDto.getEndTime());
    SpareTime spareTime = spareTimeRepository.findById(spareTimeDto.getId())
        .orElseThrow(() -> new IllegalArgumentException("해당 ID의 자투리 시간 설정이 존재하지 않습니다."));
    spareTime.setTitle(spareTimeDto.getTitle());
    spareTime.setStartTime(spareTimeDto.getStartTime());
    spareTime.setEndTime(spareTimeDto.getEndTime());
    spareTime.setDuration(Duration.between(startTime, endTime).toMinutes());
    spareTime.setDays(spareTimeDto.getDays()); // 헬퍼 메서드 사용
    spareTimeRepository.save(spareTime);
  }

  public void delete(Long spareTimeId) {
    spareTimeRepository.deleteById(spareTimeId);
  }
}
