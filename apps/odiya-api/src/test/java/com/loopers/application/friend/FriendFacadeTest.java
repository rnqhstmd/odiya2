package com.loopers.application.friend;

import com.loopers.domain.friend.FriendService;
import com.loopers.domain.friend.Friendship;
import com.loopers.domain.notification.NotificationType;
import com.loopers.domain.tag.Tag;
import com.loopers.domain.tag.TagService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.application.notification.NotificationFacade;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FriendFacadeTest {

    @Mock
    private FriendService friendService;

    @Mock
    private UserService userService;

    @Mock
    private TagService tagService;

    @Mock
    private NotificationFacade notificationFacade;

    @InjectMocks
    private FriendFacade friendFacade;

    @DisplayName("sendFriendRequest 테스트")
    @Nested
    class SendFriendRequest {

        @DisplayName("정상: 친구 요청 후 알림이 발송된다.")
        @Test
        void sendFriendRequest_정상() {
            // arrange
            long requesterId = 1L;
            long targetUserId = 2L;

            User requester = mock(User.class);
            User receiver = mock(User.class);
            given(requester.getNickname()).willReturn("홍길동");

            given(userService.getUser(requesterId)).willReturn(requester);
            given(userService.getUser(targetUserId)).willReturn(receiver);
            given(friendService.sendRequest(requester, receiver)).willReturn(mock(Friendship.class));

            // act
            friendFacade.sendFriendRequest(requesterId, targetUserId);

            // assert
            verify(friendService).sendRequest(requester, receiver);
            verify(notificationFacade).sendNotification(
                eq(receiver), eq(requester), eq(NotificationType.FRIEND_REQUEST),
                anyString(), anyString(), any(), any());
        }

        @DisplayName("알림 실패 시 요청은 성공: 알림 예외가 전파되지 않는다.")
        @Test
        void sendFriendRequest_알림실패시_요청은성공() {
            // arrange
            long requesterId = 1L;
            long targetUserId = 2L;

            User requester = mock(User.class);
            User receiver = mock(User.class);
            given(requester.getNickname()).willReturn("홍길동");

            given(userService.getUser(requesterId)).willReturn(requester);
            given(userService.getUser(targetUserId)).willReturn(receiver);
            given(friendService.sendRequest(requester, receiver)).willReturn(mock(Friendship.class));
            willThrow(new RuntimeException("알림 오류"))
                .given(notificationFacade).sendNotification(any(), any(), any(), anyString(), anyString(), any(), any());

            // act & assert - 예외가 전파되지 않아야 함
            assertDoesNotThrow(() -> friendFacade.sendFriendRequest(requesterId, targetUserId));
            verify(friendService).sendRequest(requester, receiver);
        }
    }

    @DisplayName("acceptRequest 테스트")
    @Nested
    class AcceptRequest {

        @DisplayName("정상: 수락 + 양측 기본 태그 할당 + 알림 발송이 된다.")
        @Test
        void acceptRequest_정상() {
            // arrange
            long requestId = 5L;
            long userId = 2L;

            User requester = mock(User.class);
            given(requester.getId()).willReturn(1L);
            User accepter = mock(User.class);
            given(accepter.getId()).willReturn(userId);
            given(accepter.getNickname()).willReturn("김철수");

            Friendship friendship = mock(Friendship.class);
            given(friendship.getRequester()).willReturn(requester);
            given(friendship.getReceiver()).willReturn(accepter);

            Tag requesterTag = mock(Tag.class);
            Tag receiverTag = mock(Tag.class);

            given(friendService.getFriendship(requestId)).willReturn(friendship);
            given(tagService.getDefaultTag(1L)).willReturn(requesterTag);
            given(tagService.getDefaultTag(userId)).willReturn(receiverTag);

            // act
            friendFacade.acceptRequest(requestId, userId);

            // assert
            verify(friendService).acceptRequest(requestId, userId);
            verify(friendship).changeRequesterTag(requesterTag);
            verify(friendship).changeReceiverTag(receiverTag);
            verify(notificationFacade).sendNotification(
                eq(requester), eq(accepter), eq(NotificationType.FRIEND_ACCEPTED),
                anyString(), anyString(), any(), anyString());
        }
    }

    @DisplayName("getMyFriends 테스트")
    @Nested
    class GetMyFriends {

        @DisplayName("tagId 있을 때 소유자 검증: 올바른 태그로 조회하면 정상 반환한다.")
        @Test
        void getMyFriends_태그필터() {
            // arrange
            long userId = 1L;
            long tagId = 10L;

            User owner = mock(User.class);
            given(owner.getId()).willReturn(userId);

            Tag tag = mock(Tag.class);
            given(tag.getOwner()).willReturn(owner);
            given(tagService.getTag(tagId)).willReturn(tag);
            given(friendService.getAcceptedFriends(userId, tagId)).willReturn(List.of());

            // act
            List<FriendInfo> result = friendFacade.getMyFriends(userId, tagId);

            // assert
            verify(tagService).getTag(tagId);
            verify(friendService).getAcceptedFriends(userId, tagId);
            assertThat(result).isEmpty();
        }

        @DisplayName("잘못된 태그 - NOT_FOUND: 타인 태그로 조회 시 예외가 발생한다.")
        @Test
        void getMyFriends_잘못된태그_NOT_FOUND() {
            // arrange
            long userId = 1L;
            long tagId = 10L;

            User otherOwner = mock(User.class);
            given(otherOwner.getId()).willReturn(99L); // 다른 사용자 소유

            Tag tag = mock(Tag.class);
            given(tag.getOwner()).willReturn(otherOwner);
            given(tagService.getTag(tagId)).willReturn(tag);

            // act
            CoreException ex = assertThrows(CoreException.class,
                () -> friendFacade.getMyFriends(userId, tagId));

            // assert
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("changeFriendTag 테스트")
    @Nested
    class ChangeFriendTag {

        @DisplayName("정상: 태그 변경이 위임된다.")
        @Test
        void changeFriendTag_정상() {
            // arrange
            long userId = 1L;
            long friendUserId = 2L;
            long tagId = 10L;

            User owner = mock(User.class);
            given(owner.getId()).willReturn(userId);

            Tag tag = mock(Tag.class);
            given(tag.getOwner()).willReturn(owner);
            given(tagService.getTag(tagId)).willReturn(tag);

            // act
            friendFacade.changeFriendTag(userId, friendUserId, tagId);

            // assert
            verify(friendService).changeFriendTag(userId, friendUserId, tag);
        }

        @DisplayName("타인 태그 - NOT_FOUND: 타인의 태그로 변경 시 예외가 발생한다.")
        @Test
        void changeFriendTag_타인태그_NOT_FOUND() {
            // arrange
            long userId = 1L;
            long friendUserId = 2L;
            long tagId = 10L;

            User otherOwner = mock(User.class);
            given(otherOwner.getId()).willReturn(99L);

            Tag tag = mock(Tag.class);
            given(tag.getOwner()).willReturn(otherOwner);
            given(tagService.getTag(tagId)).willReturn(tag);

            // act
            CoreException ex = assertThrows(CoreException.class,
                () -> friendFacade.changeFriendTag(userId, friendUserId, tagId));

            // assert
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
