package com.loopers.infrastructure.fcm;

import java.util.List;

public record FcmSendResult(
    int successCount,
    int failureCount,
    List<String> failedTokens,
    List<String> invalidTokens
) {}
