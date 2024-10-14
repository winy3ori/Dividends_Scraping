package example.dividends_project.service;

import example.dividends_project.model.Auth;
import example.dividends_project.model.MemberDto;
import example.dividends_project.persist.entity.MemberEntity;
import example.dividends_project.persist.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return this.memberRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
//    }

    @Override
    public MemberDto loadUserByUsername(String username) {
        Optional<MemberEntity> member = memberRepository.findByUsername(username);
        return member.map(MemberEntity::toDto).orElse(null); // Optional을 안전하게 처리
    }


    // 회원가입
    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var result = this.memberRepository.save(member.toEntity());
        return result;
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(()-> new RuntimeException("존재하지 않는 ID 입니다."));

        // 가져온 비밀번호와 인코딩된 비밀번호 매칭
        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
}
