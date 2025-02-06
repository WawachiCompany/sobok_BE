package com.apple.sobok.member.point;

import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user/point")
public class PointController {
    private final PointLogRepository pointLogRepository;
    private final MemberService memberService;

    @GetMapping("/log")
    public ResponseEntity<?> getPointLogs(@RequestParam(required=false) LocalDate startDate, @RequestParam(required=false) LocalDate endDate) {
        Member member = memberService.getMember();
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        if(startDate == null || endDate == null) {
            startDateTime = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            endDateTime = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();
        }
        else {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atStartOfDay().plusDays(1);
        }

        List<PointLog> pointLogList = pointLogRepository.findByMemberAndCreatedAtBetween(member, startDateTime, endDateTime);
        if(pointLogList.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "해당 기간의 내역이 존재하지 않습니다."));
        }
        List<PointLogDto> pointLogDtoList = pointLogList.stream()
                .map(pointLog -> new PointLogDto(pointLog.getId(), pointLog.getPoint(), pointLog.getBalance(), pointLog.getCategory(), pointLog.getDescription(), pointLog.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pointLogDtoList);
    }
}
