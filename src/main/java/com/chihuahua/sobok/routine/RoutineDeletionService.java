package com.chihuahua.sobok.routine;

// 루틴 삭제를 위한 별도 서비스 클래스
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineDeletionService {
    private final EntityManager entityManager;
    private final Logger logger = LoggerFactory.getLogger(RoutineDeletionService.class);
    
    // 별도의 트랜잭션으로 삭제 처리
    @Transactional
    public void deleteRoutineCompletely(Long routineId) {
        logger.info("Starting deletion of routine with ID: {}", routineId);
        
        try {
            // 모든 관련 데이터 삭제 (순서 중요)
            deleteRoutineRelatedData(routineId);
            
            // 영속성 컨텍스트 초기화 (중요)
            entityManager.flush();
            entityManager.clear();
            
            logger.info("Successfully deleted routine with ID: {}", routineId);
        } catch (Exception e) {
            logger.error("Error during routine deletion: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void deleteRoutineRelatedData(Long routineId) {
        // 1. TodoLog 삭제
        int todoLogCount = entityManager.createNativeQuery(
            "DELETE FROM todo_log WHERE todo_id IN (SELECT id FROM todo WHERE routine_id = :id)")
            .setParameter("id", routineId)
            .executeUpdate();
        logger.debug("Deleted {} todo logs", todoLogCount);
        
        // 2. RoutineLog 삭제
        int routineLogCount = entityManager.createNativeQuery(
            "DELETE FROM routine_log WHERE routine_id = :id")
            .setParameter("id", routineId)
            .executeUpdate();
        logger.debug("Deleted {} routine logs", routineLogCount);
        
        // 3. Todo 삭제
        int todoCount = entityManager.createNativeQuery(
            "DELETE FROM todo WHERE routine_id = :id")
            .setParameter("id", routineId)
            .executeUpdate();
        logger.debug("Deleted {} todos", todoCount);
        
        // 4. RoutineDays 삭제
        int daysCount = entityManager.createNativeQuery(
            "DELETE FROM routine_days WHERE routine_id = :id")
            .setParameter("id", routineId)
            .executeUpdate();
        logger.debug("Deleted {} routine days", daysCount);
        
        // 5. Account에서 루틴 제거 (관계 해제)
        entityManager.createNativeQuery(
            "UPDATE routine SET account_id = NULL WHERE id = :id")
            .setParameter("id", routineId)
            .executeUpdate();
        logger.debug("Detached routine from account");
        
        // 6. 루틴 자체 삭제
        int routineCount = entityManager.createNativeQuery(
            "DELETE FROM routine WHERE id = :id")
            .setParameter("id", routineId)
            .executeUpdate();
        logger.debug("Deleted {} routines", routineCount);
    }
}