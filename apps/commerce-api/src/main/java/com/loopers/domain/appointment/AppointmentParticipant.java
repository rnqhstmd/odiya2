package com.loopers.domain.appointment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.user.User;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointment_participants")
public class AppointmentParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ParticipantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, length = 20)
    private TransportType transportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_place_id")
    private DeparturePlace departurePlace;

    protected AppointmentParticipant() {}

    @Override
    protected void guard() {
        if (this.status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "참여자 상태는 필수입니다.");
        }
        if (this.transportType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이동수단은 필수입니다.");
        }
    }

    private AppointmentParticipant(Appointment appointment, User user, ParticipantStatus status,
                                   TransportType transportType, DeparturePlace departurePlace) {
        this.appointment = appointment;
        this.user = user;
        this.status = status;
        this.transportType = transportType;
        this.departurePlace = departurePlace;
    }

    public static AppointmentParticipant create(Appointment appointment, User user,
                                                ParticipantStatus status, TransportType transportType,
                                                DeparturePlace departurePlace) {
        return new AppointmentParticipant(appointment, user, status, transportType, departurePlace);
    }

    public void accept() {
        if (this.status != ParticipantStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "대기 중인 초대만 수락할 수 있습니다.");
        }
        this.status = ParticipantStatus.ACCEPTED;
    }

    public void reject() {
        if (this.status != ParticipantStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "대기 중인 초대만 거절할 수 있습니다.");
        }
        this.status = ParticipantStatus.REJECTED;
    }

    public void updateDeparture(DeparturePlace departurePlace, TransportType transportType) {
        if (departurePlace != null) {
            this.departurePlace = departurePlace;
        }
        if (transportType != null) {
            this.transportType = transportType;
        }
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public User getUser() {
        return user;
    }

    public ParticipantStatus getStatus() {
        return status;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public DeparturePlace getDeparturePlace() {
        return departurePlace;
    }
}
