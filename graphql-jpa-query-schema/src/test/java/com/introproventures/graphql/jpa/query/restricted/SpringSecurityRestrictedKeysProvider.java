package com.introproventures.graphql.jpa.query.restricted;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.introproventures.graphql.jpa.query.schema.RestrictedKeysProvider;
import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector.EntityIntrospectionResult;
import jakarta.persistence.metamodel.Metamodel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringSecurityRestrictedKeysProvider implements RestrictedKeysProvider {
    
    private final Metamodel metamodel;
    
    public SpringSecurityRestrictedKeysProvider(Metamodel metamodel) {
        this.metamodel = metamodel;
    }

    @Override
    public Optional<List<Object>> apply(EntityIntrospectionResult entityDescriptor) {
        Authentication token = SecurityContextHolder.getContext()
                                                    .getAuthentication();
        if (token == null) {
            return Optional.empty();
        }

        String entityName = metamodel.entity(entityDescriptor.getEntity())
                                     .getName();

        List<Object> keys = resolveKeys(entityName, token.getAuthorities());

        return keys.isEmpty() ? Optional.empty() // restrict query if no permissions exists 
                              : Optional.of(keys); // execute query with restricted keys
    }
    
    private List<Object> resolveKeys(String entityName, Collection<? extends GrantedAuthority> grantedAuthorities) {
        return grantedAuthorities.stream()
                                 .filter(GrantedAuthority.class::isInstance)
                                 .map(GrantedAuthority.class::cast)
                                 .map(GrantedAuthority::getAuthority)
                                 .map(authority -> authority.split(":"))
                                 .filter(permission -> entityName.equals(permission[0]))
                                 .filter(permission -> "read".equals(permission[1]))
                                 .map(permission -> permission[2])
                                 .collect(Collectors.toList());

    }
    
}
