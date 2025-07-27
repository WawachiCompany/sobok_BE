package com.chihuahua.sobok.Firebase;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class PushNotificationScheduler {

  private final RoutineRepository routineRepository;
  private final FcmTokenRepository fcmTokenRepository;
  private final FirebaseService firebaseService;

  @Scheduled(cron = "0 0 9,15,21 * * ?")
  @Transactional
  public void sendPushNotification() {
    List<FcmToken> fcmTokens = fcmTokenRepository.findAll();
    for (FcmToken fcmToken : fcmTokens) {
      Member member = fcmToken.getMember();
      LocalDate date = LocalDate.now();
      String dayOfWeek = date.getDayOfWeek().toString();
      List<Routine> routines = routineRepository.findByUserIdAndDay(member.getId(), dayOfWeek);
      List<Routine> notAchievedRoutines = routines.stream() // 오늘 완료하지 않은 루틴 필터링
          .filter(routine -> !routine.getIsAchieved())
          .toList();
      if (notAchievedRoutines.isEmpty()) {
        continue; // 오늘 완료한 루틴이 없으면 알림 전송하지 않음
      }

      String token = fcmToken.getFcmToken();
      String title = "아직 완료하지 않은 루틴이 있어요!";
      String body = "빨리 완료하고 오늘의 루틴을 달성해보세요!";

      firebaseService.sendMessage(token, title, body);
    }

  }
}
