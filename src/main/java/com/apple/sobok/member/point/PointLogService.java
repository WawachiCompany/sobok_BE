package com.apple.sobok.member.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointLogService {
    private final PointLogRepository pointLogRepository;


    public void save(PointLog pointLog) {
        pointLogRepository.save(pointLog);
    }
}
