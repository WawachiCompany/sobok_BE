package com.chihuahua.sobok.Firebase;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
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

    // 현재 시간을 가져와서 시간대별 메시지 설정
    int currentHour = LocalTime.now().getHour();
    String title;
    String body;

    // 시간대별로 다른 메시지 설정
    if (currentHour == 9) {
      title = "아침 해가 밝았어요!";
      body = "상쾌하게 시작해볼까요?";
    } else if (currentHour == 15) {
      title = "나른한 오후 … 이대로는 안되겠어요!";
      body = "소복과 함께 잠 깨러 가보시죠!";
    } else {  // 21시
      title = "오늘 하루도 수고하셨어요!";
      body = "최고의 하루를 만들기 위해! 마지막까지 달려볼까요?";
    }

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
      firebaseService.sendMessage(token, title, body);
    }
  }
}
