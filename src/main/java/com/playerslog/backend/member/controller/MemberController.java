package com.playerslog.backend.member.controller;

import com.playerslog.backend.member.dto.MemberProfileResponse;
import com.playerslog.backend.member.entity.Member;
import com.playerslog.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> getMyProfile(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(MemberProfileResponse.from(member));
    }

/*    @PatchMapping("/me")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid MemberUpdateDto memberUpdateDto) {
        memberService.updateMyProfile(member, memberUpdateDto);
        return ResponseEntity.ok().build();
    }*/
}
