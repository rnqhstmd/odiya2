package com.loopers.application.common;

/**
 * Facade 계층에서 반복적으로 사용되는 에러 메시지 상수.
 * 같은 메시지가 여러 Facade에 하드코딩되는 것을 방지한다.
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    // NOT_FOUND
    public static final String TAG_NOT_FOUND = "존재하지 않는 태그입니다.";
    public static final String DEPARTURE_PLACE_NOT_FOUND = "존재하지 않는 출발지입니다.";
    public static final String APPOINTMENT_PARTICIPANT_NOT_FOUND = "해당 약속의 참여자가 아닙니다.";

    // BAD_REQUEST
    public static final String INVITE_REQUIRES_FRIENDSHIP = "친구 관계인 사용자만 초대할 수 있습니다.";
}
