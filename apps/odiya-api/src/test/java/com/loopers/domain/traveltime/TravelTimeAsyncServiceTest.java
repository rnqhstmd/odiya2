package com.loopers.domain.traveltime;

import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentParticipantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TravelTimeAsyncServiceTest {

    @Mock
    private AppointmentParticipantRepository participantRepository;

    @Mock
    private TravelTimeService travelTimeService;

    @InjectMocks
    private TravelTimeAsyncService travelTimeAsyncService;

    @DisplayName("handleTravelTimeCalculation를 호출할 때,")
    @Nested
    class HandleTravelTimeCalculation {

        @DisplayName("참여자별 이동시간을 계산한다.")
        @Test
        void handleTravelTimeCalculation_참여자별_이동시간을_계산한다() {
            // arrange
            AppointmentParticipant participant1 = mock(AppointmentParticipant.class);
            AppointmentParticipant participant2 = mock(AppointmentParticipant.class);
            given(participantRepository.findById(1L)).willReturn(Optional.of(participant1));
            given(participantRepository.findById(2L)).willReturn(Optional.of(participant2));

            TravelTimeCalculateEvent event = new TravelTimeCalculateEvent(List.of(1L, 2L));

            // act
            travelTimeAsyncService.handleTravelTimeCalculation(event);

            // assert
            verify(travelTimeService).calculateAndSave(participant1);
            verify(travelTimeService).calculateAndSave(participant2);
        }

        @DisplayName("참여자가 없으면 건너뛴다.")
        @Test
        void handleTravelTimeCalculation_참여자가_없으면_건너뛴다() {
            // arrange
            given(participantRepository.findById(999L)).willReturn(Optional.empty());

            TravelTimeCalculateEvent event = new TravelTimeCalculateEvent(List.of(999L));

            // act
            travelTimeAsyncService.handleTravelTimeCalculation(event);

            // assert
            verify(travelTimeService, never()).calculateAndSave(any());
        }

        @DisplayName("계산실패시 다음참여자를 계속처리한다.")
        @Test
        void handleTravelTimeCalculation_계산실패시_다음참여자를_계속처리한다() {
            // arrange
            AppointmentParticipant participant1 = mock(AppointmentParticipant.class);
            AppointmentParticipant participant2 = mock(AppointmentParticipant.class);
            given(participantRepository.findById(1L)).willReturn(Optional.of(participant1));
            given(participantRepository.findById(2L)).willReturn(Optional.of(participant2));
            given(travelTimeService.calculateAndSave(participant1)).willThrow(new RuntimeException("계산 실패"));

            TravelTimeCalculateEvent event = new TravelTimeCalculateEvent(List.of(1L, 2L));

            // act
            travelTimeAsyncService.handleTravelTimeCalculation(event);

            // assert: participant1 실패 후에도 participant2 계산이 수행됨
            verify(travelTimeService).calculateAndSave(participant1);
            verify(travelTimeService).calculateAndSave(participant2);
        }
    }
}
