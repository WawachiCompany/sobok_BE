package com.apple.sobok.statistics.report;

import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SnowCardController {

    private final SnowCardService snowCardService;
    private final MemberService memberService;

    @GetMapping("/snowcard")
    public ResponseEntity<?> getSnowCard() {
        Member member = memberService.getMember();
        return ResponseEntity.ok(snowCardService.getSnowCard(member));
    }
}
