package com.beyond.ordersystem.member.Controller;

import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import com.beyond.ordersystem.common.dto.CommonDto;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.*;
import com.beyond.ordersystem.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<?> createMember(@RequestBody @Valid MemberCreateDto dto) {
        Long id = memberService.createMember(dto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id+"번째 회원")
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("회원가입 완료")
                        .build(),
                HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody @Valid LoginReqDto dto) {
        Member member = memberService.doLogin(dto);
//        at 토큰 생성
        String accessToken = jwtTokenProvider.createAtToken(member);
//        rt 토큰 생성
        String refreshToken = jwtTokenProvider.createRtToken(member);

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(LoginResDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build())
                        .status_code(HttpStatus.OK.value())
                        .status_message("로그인 성공")
                        .build(),
                HttpStatus.OK
        );
    }

//    rt를 통한 at 갱신 요청
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
//        rt 검증 로직
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());
//        at 신규 생성 로직
        String accessToken = jwtTokenProvider.createAtToken(member);

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(LoginResDto.builder()
                                .accessToken(accessToken)
                                .build())
                        .status_code(HttpStatus.OK.value())
                        .status_message("토큰 재발급 완료")
                        .build(),
                HttpStatus.OK

        );
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        List<MemberResDto> members = memberService.findAll();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(members)
                        .status_code(HttpStatus.OK.value())
                        .status_message("조회 성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        System.out.println(12);
        MemberResDto member = memberService.findById(id);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(member)
                        .status_code(HttpStatus.OK.value())
                        .status_message("조회 성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("myinfo")
    public ResponseEntity<?> myInfo() {
        MemberResDto memberResDto = memberService.myinfo();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberResDto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("내 정보 조회 성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete() {
        Long id = memberService.delete();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id+"번 회원 탈퇴")
                        .status_code(HttpStatus.OK.value())
                        .status_message("회원 탈퇴 성공")
                        .build(),
                HttpStatus.OK
        );
    }
}
