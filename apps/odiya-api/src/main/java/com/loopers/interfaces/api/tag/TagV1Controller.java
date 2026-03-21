package com.loopers.interfaces.api.tag;

import com.loopers.application.tag.TagFacade;
import com.loopers.application.tag.TagInfo;
import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tags")
public class TagV1Controller implements TagV1ApiSpec {
    private final TagFacade tagFacade;

    @GetMapping
    @Override
    public ApiResponse<List<TagV1Dto.TagResponse>> getMyTags(@AuthenticationPrincipal LoginUser loginUser) {
        List<TagV1Dto.TagResponse> response = tagFacade.getMyTags(loginUser.userId()).stream()
            .map(TagV1Dto.TagResponse::from).toList();
        return ApiResponse.success(response);
    }

    @PostMapping
    @Override
    public ApiResponse<TagV1Dto.TagResponse> createTag(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody TagV1Dto.CreateTagRequest request
    ) {
        TagInfo info = tagFacade.createTag(loginUser.userId(), request.name(), request.color());
        return ApiResponse.success(TagV1Dto.TagResponse.from(info));
    }

    @PatchMapping("/{tagId}")
    @Override
    public ApiResponse<TagV1Dto.TagResponse> updateTag(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long tagId,
        @Valid @RequestBody TagV1Dto.UpdateTagRequest request
    ) {
        TagInfo info = tagFacade.updateTag(loginUser.userId(), tagId, request.name(), request.color());
        return ApiResponse.success(TagV1Dto.TagResponse.from(info));
    }

    @DeleteMapping("/{tagId}")
    @Override
    public ApiResponse<Void> deleteTag(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long tagId) {
        tagFacade.deleteTag(loginUser.userId(), tagId);
        return ApiResponse.<Void>success();
    }
}
