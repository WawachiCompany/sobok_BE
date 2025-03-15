package com.chihuahua.sobok.statistics.report;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SnowCardController {

    private final SnowCardService snowCardService;
    private final MemberService memberService;

    @GetMapping("/snowcard")
    public ResponseEntity<?> getSnowCard(@RequestParam String yearMonth) {
        Member member = memberService.getMember();
        return ResponseEntity.ok(snowCardService.getSnowCard(member, yearMonth));
    }

    @GetMapping("/snowcard/all")
    public ResponseEntity<?> getAllSnowCard() {
        Member member = memberService.getMember();
        return ResponseEntity.ok(snowCardService.getAllSnowCard(member));
    }
}
