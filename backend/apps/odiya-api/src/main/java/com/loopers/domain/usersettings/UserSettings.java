package com.loopers.domain.usersettings;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_settings")
public class UserSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_transport_type", nullable = false)
    private TransportType defaultTransportType;

    @Column(name = "parking_buffer_minutes", nullable = false)
    private Integer parkingBufferMinutes;

    @Column(name = "extra_minutes", nullable = false)
    private Integer extraMinutes;

    protected UserSettings() {}

    private UserSettings(User user, TransportType defaultTransportType,
                         Integer parkingBufferMinutes, Integer extraMinutes) {
        this.user = user;
        this.defaultTransportType = defaultTransportType;
        this.parkingBufferMinutes = parkingBufferMinutes;
        this.extraMinutes = extraMinutes;
    }

    public static UserSettings createDefault(User user) {
        return new UserSettings(user, TransportType.TRANSIT, 10, 5);
    }

    @Override
    protected void guard() {
        guardMinutes(this.parkingBufferMinutes, "주차 버퍼 시간");
        guardMinutes(this.extraMinutes, "추가 여유 시간");
    }

    public void update(TransportType defaultTransportType,
                       Integer parkingBufferMinutes, Integer extraMinutes) {
        if (defaultTransportType != null) {
            this.defaultTransportType = defaultTransportType;
        }
        if (parkingBufferMinutes != null) {
            guardMinutes(parkingBufferMinutes, "주차 버퍼 시간");
            this.parkingBufferMinutes = parkingBufferMinutes;
        }
        if (extraMinutes != null) {
            guardMinutes(extraMinutes, "추가 여유 시간");
            this.extraMinutes = extraMinutes;
        }
    }

    public User getUser() {
        return user;
    }

    public TransportType getDefaultTransportType() {
        return defaultTransportType;
    }

    public Integer getParkingBufferMinutes() {
        return parkingBufferMinutes;
    }

    public Integer getExtraMinutes() {
        return extraMinutes;
    }

    private void guardMinutes(Integer minutes, String fieldName) {
        if (minutes == null || minutes < 0 || minutes > 60) {
            throw new CoreException(ErrorType.BAD_REQUEST,
                fieldName + "은(는) 0~60 범위여야 합니다.");
        }
    }
}
