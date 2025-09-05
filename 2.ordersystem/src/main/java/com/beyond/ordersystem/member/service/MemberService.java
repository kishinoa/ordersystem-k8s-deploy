package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.common.auth.SecurityConfig;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.LoginReqDto;
import com.beyond.ordersystem.member.dto.MemberCreateDto;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long createMember(@Valid MemberCreateDto dto) {
        if(memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("존재하는 이메일입니다.");
        }

        String EncodedPassword = passwordEncoder.encode(dto.getPassword());
        Member member = memberRepository.save(dto.toEntity(EncodedPassword));
        return member.getId();
    }

    public Member doLogin(@Valid LoginReqDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new IllegalArgumentException("로그인 실패"));

        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("로그인 실패");
        }

        return member;
    }

    public List<MemberResDto> findAll() {
        return memberRepository.findAll().stream().map(a -> MemberResDto.fromEntity(a)).collect(Collectors.toList());
    }

    public MemberResDto myinfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("등록된 계정 없음"));
        return MemberResDto.fromEntity(member);
    }

    public Long delete() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("등록된 계정 없음"));
        member.deleteMember();
        return member.getId();
    }

    public MemberResDto findById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 id"));
        return MemberResDto.fromEntity(member);
    }
}
