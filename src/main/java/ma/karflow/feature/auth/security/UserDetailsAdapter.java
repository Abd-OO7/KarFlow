package ma.karflow.feature.auth.security;

import lombok.Getter;
import ma.karflow.feature.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Adapts our User entity to Spring Security's UserDetails interface.
 */
@Getter
public class UserDetailsAdapter implements UserDetails {

    private final UUID userId;
    private final UUID tenantId;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Set<GrantedAuthority> authorities;
    private final Set<String> roleNames;
    private final Set<String> permissionNames;

    public UserDetailsAdapter(User user) {
        this.userId = user.getId();
        this.tenantId = user.getTenantId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();

        this.roleNames = new HashSet<>();
        this.permissionNames = new HashSet<>();
        this.authorities = new HashSet<>();

        user.getRoles().forEach(role -> {
            String roleName = role.getName().name();
            roleNames.add(roleName);
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

            role.getPermissions().forEach(permission -> {
                permissionNames.add(permission.getName());
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            });
        });
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
