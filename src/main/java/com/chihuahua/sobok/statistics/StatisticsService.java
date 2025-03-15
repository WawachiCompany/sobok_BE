package com.chihuahua.sobok.statistics;


import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineLog;
import com.chihuahua.sobok.routine.RoutineLogRepository;
import com.chihuahua.sobok.routine.RoutineRepository;
import com.chihuahua.sobok.routine.todo.TodoLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
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
    private final RoutineRepository routineRepository;
    private final TodoLogRepository todoLogRepository;

    public List<DailyAchieveDto> getDailyAchieve(Member member, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        List<DailyAchieve> dailyAchieves = dailyAchieveRepository.findByMemberAndDateBetween(member, start, end);
        return dailyAchieves.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public DailyAchieveDto convertToDto(DailyAchieve dailyAchieve) {
        DailyAchieveDto dto = new DailyAchieveDto();
        dto.setDate(dailyAchieve.getDate().toString());
        dto.setStatus(dailyAchieve.getStatus());
        return dto;
    }

    public List<?> getDailyRoutineAchieve(Long routineId, String startDate, String endDate) {
        Routine routine = routineRepository.findById(routineId).orElseThrow(
                () -> new IllegalArgumentException("해당 ID의 루틴이 존재하지 않습니다."));
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        return start.datesUntil(end.plusDays(1))
                    .map(date -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", date);
                        List<RoutineLog> routineLogs = routineLogRepository.findByRoutineAndEndTimeBetween(routine, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
                        boolean isToday = routine.getDays().contains(date.getDayOfWeek().name()); // 오늘이 루틴하는 날인지 판단
                        boolean achieved = routineLogs.isEmpty();
                        if(!isToday) {
                            map.put("status", "NO_ROUTINE");
                        } else if(achieved) {
                            map.put("status", "NONE_ACHIEVED");
                        } else {
                            map.put("status", "ACHIEVED");
                        }
                        return map;
                    })
                    .collect(Collectors.toList());
    }

    public List<?> getDailyAchieveLog(Member member, String date){
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        return routineRepository.findByMember(member).stream()
                .filter(routine -> routineLogRepository.findByRoutineAndIsCompletedAndEndTimeBetween(
                        routine, true, localDate.atStartOfDay(), localDate.plusDays(1).atStartOfDay()
                ).isPresent())
                .map(routine -> {
                    Map<String, Object> map = new HashMap<>();
                    RoutineLog routineLog = routineLogRepository.findByRoutineAndIsCompletedAndEndTimeBetween(routine, true, localDate.atStartOfDay(), localDate.plusDays(1).atStartOfDay()).get();
                        map.put("title", routine.getTitle());
                        map.put("accountTitle", routine.getAccount().getTitle());
                        map.put("duration", routineLog.getDuration());
                        map.put("startTime", routineLog.getStartTime());
                        map.put("endTime", routineLog.getEndTime());
                        List<Map<String, Object>> todoLogs = routine.getTodos().stream()
                                .flatMap(todo -> todoLogRepository.findByTodoAndEndTimeBetween(todo, localDate.atStartOfDay(), localDate.plusDays(1).atStartOfDay()).stream()
                                        .map(todoLog -> {
                                            Map<String, Object> todoMap = new HashMap<>();
                                            todoMap.put("title", todo.getTitle());
                                            todoMap.put("linkApp", todo.getLinkApp());
                                            todoMap.put("duration", todoLog.getDuration());
                                            return todoMap;
                                        }))
                                .collect(Collectors.toList());
                        map.put("todoLogs", todoLogs);
                    return map;
                })
                .toList();
    }

    public List<?> getDailyRoutineAchieveLog(Long routineId, String date){
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        Routine routine = routineRepository.findById(routineId).orElseThrow(
                () -> new IllegalArgumentException("해당 ID의 루틴이 존재하지 않습니다."));
        return routine.getTodos().stream()
                .flatMap(todo -> todoLogRepository.findByTodoAndEndTimeBetween(todo, localDate.atStartOfDay(), localDate.plusDays(1).atStartOfDay()).stream()
                        .map(todoLog -> {
                            Map<String, Object> todoMap = new HashMap<>();
                            todoMap.put("title", todo.getTitle());
                            todoMap.put("linkApp", todo.getLinkApp());
                            todoMap.put("duration", todoLog.getDuration());
                            return todoMap;
                        }))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDateTimeStatistics(Member member, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        List<Routine> routines = routineRepository.findByMember(member);
        long totalDuration = routines.stream()
                .mapToLong(routine -> {
                    List<RoutineLog> routineLogs = routineLogRepository.findAllByRoutineAndIsCompletedAndEndTimeBetween(routine, true, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
                    return routineLogs.stream()
                            .mapToLong(RoutineLog::getDuration)
                            .sum();
                })
                .sum();
        long totalAchievedCount = dailyAchieveRepository.findByMemberAndDateBetween(member, start, end).stream()
                .filter(dailyAchieve -> "ALL_ACHIEVED".equals(dailyAchieve.getStatus()) || "SOME_ACHIEVED".equals(dailyAchieve.getStatus()))
                .count();
        return Map.of(
                "totalDuration", totalDuration,
                "totalAchievedCount", totalAchievedCount
        );
    }

    public Map<String, Object> getRoutineTimeStatistics(Long routineId) {
        Routine routine = routineRepository.findById(routineId).orElseThrow(
                () -> new IllegalArgumentException("해당 ID의 루틴이 존재하지 않습니다."));
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = now.with(DayOfWeek.SUNDAY);
        List<RoutineLog> routineLogs = routineLogRepository.findAllByRoutineAndIsCompletedAndEndTimeBetween(routine, true, startOfWeek.atStartOfDay(), endOfWeek.plusDays(1).atStartOfDay());
        long totalDuration = routineLogs.stream()
                .mapToLong(RoutineLog::getDuration)
                .sum();
        long totalAchievedCount = routineLogs.size();
        return Map.of(
                "totalDuration", totalDuration,
                "totalAchievedCount", totalAchievedCount
        );
    }


}
