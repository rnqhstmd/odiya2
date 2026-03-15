package com.loopers.interfaces.api.notification;

import com.loopers.application.notification.NotificationFacade;
import com.loopers.application.notification.NotificationListInfo;
import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationV1Controller implements NotificationV1ApiSpec {

    private final NotificationFacade notificationFacade;

    @GetMapping
    @Override
    public ApiResponse<NotificationV1Dto.NotificationListResponse> getNotifications(
        @AuthenticationPrincipal LoginUser loginUser,
        @RequestParam(required = false) Long cursor,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        NotificationListInfo listInfo = notificationFacade.getNotifications(
            loginUser.userId(), cursor, size);
        return ApiResponse.success(NotificationV1Dto.NotificationListResponse.from(listInfo));
    }

    @PatchMapping("/{id}/read")
    @Override
    public ApiResponse<Void> markAsRead(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id
    ) {
        notificationFacade.markAsRead(id, loginUser.userId());
        return ApiResponse.success();
    }

    @PostMapping("/read-all")
    @Override
    public ApiResponse<NotificationV1Dto.ReadAllResponse> markAllAsRead(
        @AuthenticationPrincipal LoginUser loginUser
    ) {
        int count = notificationFacade.markAllAsRead(loginUser.userId());
        return ApiResponse.success(new NotificationV1Dto.ReadAllResponse(count));
    }

    @GetMapping("/unread-count")
    @Override
    public ApiResponse<NotificationV1Dto.UnreadCountResponse> getUnreadCount(
        @AuthenticationPrincipal LoginUser loginUser
    ) {
        int count = notificationFacade.getUnreadCount(loginUser.userId());
        return ApiResponse.success(new NotificationV1Dto.UnreadCountResponse(count));
    }
}
