package example.dividends_project.persist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "MEMBER")
public class MemberEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    //    @Convert    // List값 저장 위한 어노테이션, 리스트를 자동으로 StringListConverter.class 정의한 값으로 변환
    @ElementCollection(fetch = FetchType.EAGER) //fetch를 추가하니 지연 로딩이 해결됐다... 이유는 뭘까... 다음에 알아보자
    private List<String> roles;

//    public MemberDto toDto() {
//        return new MemberDto(this.username, this.roles);
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
