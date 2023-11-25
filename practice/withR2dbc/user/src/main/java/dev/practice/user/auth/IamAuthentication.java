package dev.practice.user.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;

/**
 * Spring Security Core 라이브러리의
 * Authentication 클래스를 구현한다.
 *
 * 구현의 간편함을 위해..
 * - name 필드를 생성자로 받고..
 * - getName 은 오버라이딩해서 name 필드 값을 반환
 * - isAuthenticated 는 오버라이딩해서 항상 true 반환
 * - getPrincipal 은 오버라이딩해서 새로운 Principal(Java security lib) 을 생성해서 반환
 */
@RequiredArgsConstructor
public class IamAuthentication implements Authentication {

    private final String name;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Principal getPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return name;
            }
        };
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return this.name;
    }
}
