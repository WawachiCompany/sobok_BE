package com.apple.sobok.member.spareTIme;

import com.apple.sobok.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpareTimeService {
    public final SpareTimeRepository spareTimeRepository;

    public Map<String, Object> getSpareTimeByDate(Member member, String day) {
        List<SpareTime> spareTimeList = spareTimeRepository.findByMemberAndDaysContaining(member, List.of(day));
        var result =  spareTimeList.stream()
                .map(spareTime -> {
                    SpareTimeResponseDto dto = new SpareTimeResponseDto();
                    dto.setId(spareTime.getId());
                    dto.setTitle(spareTime.getTitle());
                    dto.setStartTime(spareTime.getStartTime());
                    dto.setEndTime(spareTime.getEndTime());
                    dto.setDuration(spareTime.getDuration());
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
        List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        Map<String, Long> result = new HashMap<>();
        days.forEach(day -> {
            List<SpareTime> spareTimeList = spareTimeRepository.findByMemberAndDaysContaining(member, List.of(day));
            if(spareTimeList.isEmpty()) {
                result.put(day, 0L);
            }
            else {
                long totalDuration = spareTimeList.stream()
                        .mapToLong(SpareTime::getDuration)
                        .sum();
                result.put(day, totalDuration);
            }
        });
        return result;
    }

    public SpareTime save(Member member, SpareTimeDto spareTimeDto) {
        LocalTime startTime = LocalTime.parse(spareTimeDto.getStartTime());
        LocalTime endTime = LocalTime.parse(spareTimeDto.getEndTime());
        SpareTime spareTime = new SpareTime();
        spareTime.setTitle(spareTimeDto.getTitle());
        spareTime.setStartTime(spareTimeDto.getStartTime());
        spareTime.setEndTime(spareTimeDto.getEndTime());
        spareTime.setMember(member);
        spareTime.setDuration(Duration.between(startTime,endTime).toMinutes());
        spareTime.setDays(spareTimeDto.getDays());
        return spareTimeRepository.save(spareTime);
    }

    public void update(SpareTimeDto spareTimeDto) {
        LocalTime startTime = LocalTime.parse(spareTimeDto.getStartTime());
        LocalTime endTime = LocalTime.parse(spareTimeDto.getEndTime());
        SpareTime spareTime = spareTimeRepository.findById(spareTimeDto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 ID의 자투리 시간 설정이 존재하지 않습니다."));
        spareTime.setTitle(spareTimeDto.getTitle());
        spareTime.setStartTime(spareTimeDto.getStartTime());
        spareTime.setEndTime(spareTimeDto.getEndTime());
        spareTime.setDuration(Duration.between(startTime,endTime).toMinutes());
        spareTime.setDays(spareTimeDto.getDays());
        spareTimeRepository.save(spareTime);
    }

    public void delete(Long spareTimeId) {
        spareTimeRepository.deleteById(spareTimeId);
    }
}
