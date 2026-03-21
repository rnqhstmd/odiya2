package com.loopers.domain.traveltime;

import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelTimeAsyncService {

    private final AppointmentParticipantRepository participantRepository;
    private final TravelTimeService travelTimeService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("travelTimeExecutor")
    public void handleTravelTimeCalculation(TravelTimeCalculateEvent event) {
        for (Long participantId : event.participantIds()) {
            try {
                AppointmentParticipant participant = participantRepository.findById(participantId)
                    .orElse(null);
                if (participant == null) {
                    log.warn("비동기 이동시간 계산: 참여자를 찾을 수 없습니다. participantId={}", participantId);
                    continue;
                }
                travelTimeService.calculateAndSave(participant);
            } catch (Exception e) {
                log.warn("비동기 이동시간 계산 실패: participantId={}, error={}", participantId, e.getMessage(), e);
            }
        }
    }
}
