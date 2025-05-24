package com.chihuahua.sobok;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.todo.TodoLog;
import com.chihuahua.sobok.routine.todo.TodoLogRepository;
import com.chihuahua.sobok.statistics.report.ReportService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private TodoLogRepository todoLogRepository;

    @InjectMocks
    private ReportService reportService;

    private Member testMember;
    private YearMonth testYearMonth;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId(1L);
        testMember.setUsername("testUser");

        testYearMonth = YearMonth.of(2023, 8); // 2023년 8월
    }

    @Test
    @DisplayName("가장 많이 수행된 시작 시간을 찾을 수 있다")
    void shouldFindMostPerformedStartTime() {
        // Given
        TodoLog log1 = createTodoLog(LocalDateTime.of(2023, 8, 1, 9, 5));
        TodoLog log2 = createTodoLog(LocalDateTime.of(2023, 8, 2, 9, 10));
        TodoLog log3 = createTodoLog(LocalDateTime.of(2023, 8, 3, 9, 20));
        TodoLog log4 = createTodoLog(LocalDateTime.of(2023, 8, 4, 14, 0));
        TodoLog log5 = createTodoLog(LocalDateTime.of(2023, 8, 5, 14, 15));
        
        List<TodoLog> todoLogs = Arrays.asList(log1, log2, log3, log4, log5);
        
        when(todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
                eq(testMember), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(todoLogs);

        // When
        String result = reportService.getMostPerformedStartTime(testMember, testYearMonth);

        // Then
        assertEquals("09:00", result); // 9시에 3번, 14시에 2번 수행되었으므로 9시가 가장 많음
    }

    @Test
    @DisplayName("동일한 빈도로 수행된 시간이 여러 개 있을 경우 그 중 하나를 반환한다")
    void shouldReturnOneOfMostPerformedStartTimesWhenTied() {
        // Given
        TodoLog log1 = createTodoLog(LocalDateTime.of(2023, 8, 1, 9, 0));
        TodoLog log2 = createTodoLog(LocalDateTime.of(2023, 8, 2, 14, 0));
        
        List<TodoLog> todoLogs = Arrays.asList(log1, log2);
        
        when(todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
                eq(testMember), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(todoLogs);

        // When
        String result = reportService.getMostPerformedStartTime(testMember, testYearMonth);

        // Then
        // 9시와 14시가 각각 1번씩 수행되었으므로 둘 중 하나가 반환됨
        // 실제 구현에서는 정렬 순서에 따라 결정될 수 있음
        assertTrue(result.equals("09:00") || result.equals("14:00"));
    }

    @Test
    @DisplayName("로그가 없을 경우 'none'을 반환한다")
    void shouldReturnNoneWhenNoLogs() {
        // Given
        when(todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
                eq(testMember), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        String result = reportService.getMostPerformedStartTime(testMember, testYearMonth);

        // Then
        assertEquals("none", result);
    }

    @Test
    @DisplayName("시간이 30분 단위로 반올림되는지 확인한다")
    void shouldRoundTimeToNearestHalfHour() {
        // Given
        TodoLog log1 = createTodoLog(LocalDateTime.of(2023, 8, 1, 9, 10)); // 9:00으로 반올림
        TodoLog log2 = createTodoLog(LocalDateTime.of(2023, 8, 2, 9, 20)); // 9:30으로 반올림
        TodoLog log3 = createTodoLog(LocalDateTime.of(2023, 8, 3, 9, 40)); // 9:30으로 반올림
        
        List<TodoLog> todoLogs = Arrays.asList(log1, log2, log3);
        
        when(todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
                eq(testMember), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(todoLogs);

        // When
        String result = reportService.getMostPerformedStartTime(testMember, testYearMonth);

        // Then
        assertEquals("09:30", result); // 9:30이 2번, 9:00이 1번이므로 9:30이 가장 많음
    }

    // TodoLog 생성 헬퍼 메서드
    private TodoLog createTodoLog(LocalDateTime startTime) {
        TodoLog todoLog = new TodoLog();
        todoLog.setStartTime(startTime);
        todoLog.setEndTime(startTime.plusHours(1)); // 임의로 1시간 후 종료
        return todoLog;
    }
    
    // assertTrue 메서드 정의 (JUnit에 없는 경우를 대비)
    private void assertTrue(boolean condition) {
        Assertions.assertTrue(condition);
    }
}