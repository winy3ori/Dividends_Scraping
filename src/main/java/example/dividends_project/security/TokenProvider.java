package example.dividends_project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final long TOKEN_EXPIRED_TIME = 1000 * 60 * 60; // 1시간
    private static final String KEY_VALUE = "roles";    // 상수값은 선언해서 사용하는게 좋다


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
}
