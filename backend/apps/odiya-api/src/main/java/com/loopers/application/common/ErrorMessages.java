package com.loopers.application.common;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

/**
 * Facade 계층에서 반복적으로 사용되는 에러 메시지와 {@link ErrorType}을 결합한 enum.
 *
 * <p>이전에는 단순 {@code public final class}로 문자열 상수만 노출했지만, 그 경우 호출부가
 * {@code throw new CoreException(ErrorType.NOT_FOUND, ErrorMessages.TAG_NOT_FOUND)}와 같이
 * {@link ErrorType}과 메시지를 수동으로 짝지어야 했고, 잘못된 타입을 전달해도 컴파일러가 막지
 * 못했다. enum으로 승격해 두 값을 한 상수에 묶고, {@link #asException()} 팩토리 메서드로
 * 일관된 예외 생성을 강제한다.
 *
 * <p>사용 예:
 * <pre>{@code
 * throw ErrorMessages.TAG_NOT_FOUND.asException();
 * // Optional.orElseThrow와 함께:
 * repository.findById(id).orElseThrow(ErrorMessages.DEPARTURE_PLACE_NOT_FOUND::asException);
 * }</pre>
 */
public enum ErrorMessages {

    // NOT_FOUND
    TAG_NOT_FOUND(ErrorType.NOT_FOUND, "존재하지 않는 태그입니다."),
    DEPARTURE_PLACE_NOT_FOUND(ErrorType.NOT_FOUND, "존재하지 않는 출발지입니다."),
    APPOINTMENT_PARTICIPANT_NOT_FOUND(ErrorType.NOT_FOUND, "해당 약속의 참여자가 아닙니다."),

    // BAD_REQUEST
    INVITE_REQUIRES_FRIENDSHIP(ErrorType.BAD_REQUEST, "친구 관계인 사용자만 초대할 수 있습니다."),

    // CONFLICT
    PARTICIPANT_ALREADY_INVITED(ErrorType.CONFLICT, "이미 초대된 참여자입니다.");

    private final ErrorType errorType;
    private final String message;

    ErrorMessages(ErrorType errorType, String message) {
        this.errorType = errorType;
        this.message = message;
    }

    /** 이 에러 메시지와 결합된 {@link ErrorType}으로 {@link CoreException}을 생성한다. */
    public CoreException asException() {
        return new CoreException(errorType, message);
    }
}
