package com.loopers.domain.departureplace;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "departure_places")
public class DeparturePlace extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "label", nullable = false, length = 20)
    private String label;

    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    protected DeparturePlace() {}

    private DeparturePlace(User user, String label, String address, Double latitude, Double longitude) {
        guardLabel(label);
        guardAddress(address);
        guardLatitude(latitude);
        guardLongitude(longitude);
        this.user = user;
        this.label = label.trim();
        this.address = address.trim();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static DeparturePlace create(User user, String label, String address,
                                        Double latitude, Double longitude) {
        return new DeparturePlace(user, label, address, latitude, longitude);
    }

    @Override
    protected void guard() {
        guardLabel(this.label);
        guardAddress(this.address);
        guardLatitude(this.latitude);
        guardLongitude(this.longitude);
    }

    public void update(String label, String address, Double latitude, Double longitude) {
        if (label != null) {
            guardLabel(label);
            this.label = label.trim();
        }
        if (address != null) {
            guardAddress(address);
            this.address = address.trim();
        }
        if (latitude != null) {
            guardLatitude(latitude);
            this.latitude = latitude;
        }
        if (longitude != null) {
            guardLongitude(longitude);
            this.longitude = longitude;
        }
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    public User getUser() {
        return user;
    }

    public String getLabel() {
        return label;
    }

    public String getAddress() {
        return address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    private void guardLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "출발지 이름은 비어 있을 수 없습니다.");
        }
        if (label.trim().length() > 20) {
            throw new CoreException(ErrorType.BAD_REQUEST, "출발지 이름은 최대 20자까지 입력할 수 있습니다.");
        }
    }

    private void guardAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주소는 비어 있을 수 없습니다.");
        }
        if (address.trim().length() > 200) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주소는 최대 200자까지 입력할 수 있습니다.");
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
}
