package com.toma.blogplanet.exception;

public final class ExceptionMessages {

    public static final String BLOG_SOURCE_NOT_FOUND = "존재하지 않는 블로그 소스입니다.";
    public static final String DUPLICATE_FEED_URL = "이미 등록된 피드 URL입니다.";
    public static final String FEED_READ_FAILED = "피드를 읽는 중 오류가 발생했습니다.";
    public static final String DISCORD_NOTIFICATION_SEND_FAILED = "Discord 알림 전송 중 오류가 발생했습니다.";
    public static final String REQUEST_VALIDATION_FAILED = "요청 값 검증에 실패했습니다.";
    public static final String INTERNAL_SERVER_ERROR = "서버 내부 오류가 발생했습니다.";
    public static final String URL_REQUIRED_FOR_NORMALIZATION = "정규화할 URL이 필요합니다.";
    public static final String URL_CANNOT_BE_NORMALIZED = "정규화할 수 없는 URL입니다.";
    public static final String DUPLICATE_KEY_SOURCE_REQUIRED = "externalGuid 또는 normalizedUrl 중 하나는 필요합니다.";

    private ExceptionMessages() {
    }
}
