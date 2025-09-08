package com.beyond.ordersystem.member.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
// jpql을 제외하고 모든 조회쿼리에 where del_yn = 'N'을 붙이는 효과
@Where(clause = "del_yn = 'N'")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    @Builder.Default
    private String delYn = "N";
    @Enumerated(EnumType.STRING)
    private Role role;

    public void updatePw(String newPw) {
        this.password = newPw;
    }

    public void deleteMember() {
        this.delYn = "Y";
    }
}
