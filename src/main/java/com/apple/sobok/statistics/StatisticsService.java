package com.apple.sobok.statistics;


import com.apple.sobok.member.Member;
import com.apple.sobok.routine.Routine;
import com.apple.sobok.routine.RoutineLog;
import com.apple.sobok.routine.RoutineLogRepository;
import com.apple.sobok.routine.RoutineRepository;
import com.apple.sobok.routine.todo.TodoLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private DailyAchieveDto convertToDto(DailyAchieve dailyAchieve) {
        DailyAchieveDto dto = new DailyAchieveDto();
        dto.setDate(dailyAchieve.getDate().toString());
        dto.setStatus(dailyAchieve.getStatus());
        return  dto;
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
                        boolean achieved = routineLogs.isEmpty();
                        map.put("status", achieved ? "NONE_ACHIEVED" : "ACHIEVED");
                        return map;
                    })
                    .collect(Collectors.toList());
    }

    public List<?> getDailyAchieveLog(Member member, String date){
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        return routineRepository.findByMember(member).stream()
                .map(routine -> {
                    Map<String, Object> map = new HashMap<>();
                    Optional<RoutineLog> routineLog = routineLogRepository.findAllByRoutineAndIsCompletedAndEndTimeBetween(routine, true, localDate.atStartOfDay(), localDate.plusDays(1).atStartOfDay());
                    if(routineLog.isPresent()){
                        map.put("title", routine.getTitle());
                        map.put("accountTitle", routine.getAccount().getTitle());
                        map.put("duration", routineLog.get().getDuration());
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
                    }
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


}
