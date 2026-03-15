package com.loopers.interfaces.api.notification;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification V1 API", description = "알림 관리 API")
public interface NotificationV1ApiSpec {

    @Operation(summary = "알림 목록 조회", description = "내 알림 목록을 커서 기반 페이징으로 조회합니다.")
    ApiResponse<NotificationV1Dto.NotificationListResponse> getNotifications(
        LoginUser loginUser, Long cursor, Integer size);

    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 처리합니다.")
    ApiResponse<Void> markAsRead(LoginUser loginUser, Long id);

    @Operation(summary = "전체 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    ApiResponse<NotificationV1Dto.ReadAllResponse> markAllAsRead(LoginUser loginUser);

    @Operation(summary = "읽지 않은 알림 수 조회", description = "읽지 않은 알림 수를 조회합니다.")
    ApiResponse<NotificationV1Dto.UnreadCountResponse> getUnreadCount(LoginUser loginUser);
}
