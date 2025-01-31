package com.apple.sobok.member.point;

import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/point")
public class PointController {
    private final PointLogRepository pointLogRepository;
    private final MemberService memberService;

    @GetMapping("/log")
    public ResponseEntity<?> getPointLog() {
        Member member = memberService.getMember();

        List<PointLog> pointLogList = pointLogRepository.findByMember(member);
        if(pointLogList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<PointLogDto> pointLogDtoList = pointLogList.stream()
                .map(pointLog -> new PointLogDto(pointLog.getId(), pointLog.getPoint(), pointLog.getBalance(), pointLog.getCategory(), pointLog.getDescription(), pointLog.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pointLogDtoList);
    }
}
