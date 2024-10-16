package example.dividends_project.security;

import example.dividends_project.persist.repository.MemberRepository;
import example.dividends_project.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final long TOKEN_EXPIRED_TIME = 1000 * 60 * 60; // 1시간
    private static final String KEY_VALUE = "roles";    // 상수값은 선언해서 사용하는게 좋다

    private final MemberService memberService;

    @Value("{spring.jwt.secret}")
    private String secretKey;

    /**
     * 토큰 생성(발급)
     *
     * @param username
     * @param roles
     * @return
     */
    public String generateToken(String username, List<String> roles) {

        Claims claims = Jwts.claims().setSubject(username);
        claims.put(KEY_VALUE, roles); // key, value 형태로 저장

        var now = new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRED_TIME); // 현재 시간부터 1시간 동안 토큰이 유효

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)   // 토큰 생성 시간
                .setExpiration(expiredDate) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 암호화 알고리즘 (HS512), 비밀키
                .compact(); // builder 종료

    }

    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

//    public Authentication getAuthentication(String jwt) {
//        MemberDto memberDTO = this.memberService.loadUserByUsername(this.getUsername(jwt));
//        if (memberDTO == null) {
//            return null; // 또는 적절한 예외 처리
//        }
//        return new UsernamePasswordAuthenticationToken(memberDTO, "", memberDTO.getRoles().stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList()));
//    }

    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;

        val claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }

    // 토큰 유효한지
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
