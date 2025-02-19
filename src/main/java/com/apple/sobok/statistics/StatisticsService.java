package com.apple.sobok.statistics;


import com.apple.sobok.member.Member;
import com.apple.sobok.routine.Routine;
import com.apple.sobok.routine.RoutineLog;
import com.apple.sobok.routine.RoutineLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final DailyAchieveRepository dailyAchieveRepository;
    private final RoutineLogRepository routineLogRepository;

    public List<DailyAchieveDto> getDailyAchieve(Member member, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        List<DailyAchieve> dailyAchieves = dailyAchieveRepository.findByMemberAndDateBetween(member, start, end);
        return dailyAchieves.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private DailyAchieveDto convertToDto(DailyAchieve dailyAchieve) {
        DailyAchieveDto dto = new DailyAchieveDto();
        dto.setDate(dailyAchieve.getDate().toString());
        dto.setStatus(dailyAchieve.getStatus());
        return  dto;
    }

    public List<?> getDailyRoutineAchieve(Routine routine, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        return start.datesUntil(end.plusDays(1))
                    .map(date -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", date);
                        List<RoutineLog> routineLogs = routineLogRepository.findByRoutineAndEndTimeBetween(routine, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
                        boolean achieved = routineLogs.isEmpty();
                        map.put("status", achieved ? "NONE_ACHIEVED" : "ACHIEVED");
                        return map;
                    })
                    .collect(Collectors.toList());
    }


}
