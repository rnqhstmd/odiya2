package com.loopers.interfaces.api.tag;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Tag V1 API", description = "관계 태그 관리 API")
public interface TagV1ApiSpec {
    @Operation(summary = "내 태그 목록", description = "로그인한 사용자의 태그 목록을 조회합니다.")
    ApiResponse<List<TagV1Dto.TagResponse>> getMyTags(LoginUser loginUser);

    @Operation(summary = "태그 생성", description = "새 관계 태그를 생성합니다.")
    ApiResponse<TagV1Dto.TagResponse> createTag(LoginUser loginUser, TagV1Dto.CreateTagRequest request);

    @Operation(summary = "태그 수정", description = "태그의 이름 또는 색상을 수정합니다.")
    ApiResponse<TagV1Dto.TagResponse> updateTag(LoginUser loginUser, Long tagId, TagV1Dto.UpdateTagRequest request);

    @Operation(summary = "태그 삭제", description = "태그를 삭제합니다. 기본 태그는 삭제할 수 없습니다.")
    ApiResponse<Void> deleteTag(LoginUser loginUser, Long tagId);
}
