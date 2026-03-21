package com.loopers.domain.notification;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "device_tokens", indexes = {
    @Index(name = "uk_device_tokens_token", columnList = "token", unique = true)
})
public class DeviceToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, length = 512, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 10)
    private DeviceType deviceType;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected DeviceToken() {}

    private DeviceToken(User user, String token, DeviceType deviceType) {
        this.user = user;
        this.token = token;
        this.deviceType = deviceType;
        this.active = true;
    }

    public static DeviceToken create(User user, String token, DeviceType deviceType) {
        return new DeviceToken(user, token, deviceType);
    }

    public void deactivate() {
        this.active = false;
    }

    public void reactivate(User user) {
        this.user = user;
        this.active = true;
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    public User getUser() { return user; }
    public String getToken() { return token; }
    public DeviceType getDeviceType() { return deviceType; }
    public boolean isActive() { return active; }
}
