package com.loopers.domain.appointment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column(name = "place_address", nullable = false, length = 200)
    private String placeAddress;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "date_time", nullable = false)
    private ZonedDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentParticipant> participants = new ArrayList<>();

    protected Appointment() {}

    private Appointment(User host, String name, String placeName, String placeAddress,
                        Double latitude, Double longitude, ZonedDateTime dateTime) {
        guardName(name);
        guardPlaceName(placeName);
        guardPlaceAddress(placeAddress);
        guardLatitude(latitude);
        guardLongitude(longitude);
        guardDateTime(dateTime);
        this.host = host;
        this.name = name.trim();
        this.placeName = placeName.trim();
        this.placeAddress = placeAddress.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
        this.status = AppointmentStatus.PENDING;
    }

    public static Appointment create(User host, String name, String placeName, String placeAddress,
                                     Double latitude, Double longitude, ZonedDateTime dateTime) {
        return new Appointment(host, name, placeName, placeAddress, latitude, longitude, dateTime);
    }

    @Override
    protected void guard() {
        guardName(this.name);
        guardPlaceName(this.placeName);
        guardPlaceAddress(this.placeAddress);
        guardLatitude(this.latitude);
        guardLongitude(this.longitude);
        guardDateTime(this.dateTime);
    }

    public void update(String name, String placeName, String placeAddress,
                       Double latitude, Double longitude, ZonedDateTime dateTime) {
        if (name != null) {
            guardName(name);
            this.name = name.trim();
        }
        if (placeName != null) {
            guardPlaceName(placeName);
            this.placeName = placeName.trim();
        }
        if (placeAddress != null) {
            guardPlaceAddress(placeAddress);
            this.placeAddress = placeAddress.trim();
        }
        if (latitude != null) {
            guardLatitude(latitude);
            this.latitude = latitude;
        }
        if (longitude != null) {
            guardLongitude(longitude);
            this.longitude = longitude;
        }
        if (dateTime != null) {
            guardDateTime(dateTime);
            this.dateTime = dateTime;
        }
    }

    public void cancel() {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 취소된 약속입니다.");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.delete();
    }

    public void confirmIfAllAccepted() {
        boolean allNonRejectedAccepted = participants.stream()
            .filter(p -> p.getStatus() != ParticipantStatus.REJECTED)
            .allMatch(p -> p.getStatus() == ParticipantStatus.ACCEPTED);
        if (allNonRejectedAccepted && this.status == AppointmentStatus.PENDING) {
            this.status = AppointmentStatus.CONFIRMED;
        }
    }

    public void revertToPendingIfConfirmed() {
        if (this.status == AppointmentStatus.CONFIRMED) {
            this.status = AppointmentStatus.PENDING;
        }
    }

    public void addParticipant(AppointmentParticipant participant) {
        this.participants.add(participant);
    }

    public AppointmentParticipant findParticipant(Long userId) {
        return participants.stream()
            .filter(p -> p.getUser().getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "해당 약속의 참여자가 아닙니다."));
    }

    public boolean isHostUser(Long userId) {
        return this.host.getId().equals(userId);
    }

    public User getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public List<AppointmentParticipant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    private void guardName(String name) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "약속 이름은 비어 있을 수 없습니다.");
        }
        if (name.trim().length() > 50) {
            throw new CoreException(ErrorType.BAD_REQUEST, "약속 이름은 최대 50자까지 입력할 수 있습니다.");
        }
    }

    private void guardPlaceName(String placeName) {
        if (placeName == null || placeName.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "장소명은 비어 있을 수 없습니다.");
        }
        if (placeName.trim().length() > 100) {
            throw new CoreException(ErrorType.BAD_REQUEST, "장소명은 최대 100자까지 입력할 수 있습니다.");
        }
    }

    private void guardPlaceAddress(String placeAddress) {
        if (placeAddress == null || placeAddress.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "장소 주소는 비어 있을 수 없습니다.");
        }
        if (placeAddress.trim().length() > 200) {
            throw new CoreException(ErrorType.BAD_REQUEST, "장소 주소는 최대 200자까지 입력할 수 있습니다.");
        }
    }

    private void guardLatitude(Double latitude) {
        if (latitude == null || latitude < -90.0 || latitude > 90.0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "위도는 -90 ~ 90 범위여야 합니다.");
        }
    }

    private void guardLongitude(Double longitude) {
        if (longitude == null || longitude < -180.0 || longitude > 180.0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "경도는 -180 ~ 180 범위여야 합니다.");
        }
    }

    private void guardDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "약속 일시는 비어 있을 수 없습니다.");
        }
    }
}
