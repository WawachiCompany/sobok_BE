package com.apple.sobok.member.spareTIme;

import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class SpareTimeController {

    private final SpareTimeService spareTimeService;
    private final MemberService memberService;

    @GetMapping("/spare-time/by-day")
    public ResponseEntity<?> getSpareTimeByDate(@RequestParam String day) {
        Member member = memberService.getMember();
        return ResponseEntity.ok(spareTimeService.getSpareTimeByDate(member, day));
    }

    @GetMapping("/spare-time/duration")
    public ResponseEntity<?> getSpareTimeDuration() {
        Member member = memberService.getMember();
        return ResponseEntity.ok(spareTimeService.getSpareTimeDuration(member));
    }

    @PostMapping("/spare-time")
    public ResponseEntity<?> saveSpareTime(@RequestBody SpareTimeDto spareTimeDto) {
        try {
            Member member = memberService.getMember();
            spareTimeService.save(member, spareTimeDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("자투리 시간 등록 실패" + e.getMessage());
        }
        return ResponseEntity.ok("자투리 시간 등록 완료");
    }

    @PutMapping("/spare-time")
    public ResponseEntity<?> updateSpareTime(@RequestBody SpareTimeDto spareTimeDto) {
        try {
            spareTimeService.update(spareTimeDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("자투리 시간 수정 실패");
        }
        return ResponseEntity.ok("자투리 시간 수정 완료");
    }

    @DeleteMapping("/spare-time")
    public ResponseEntity<?> deleteSpareTime(@RequestParam Long spareTimeId) {
        try {
            spareTimeService.delete(spareTimeId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("자투리 시간 삭제 실패");
        }
        return ResponseEntity.ok("자투리 시간 삭제 완료");
    }
}
