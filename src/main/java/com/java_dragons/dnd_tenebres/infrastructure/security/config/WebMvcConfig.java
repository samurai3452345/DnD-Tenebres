package com.java_dragons.dnd_tenebres.infrastructure.security.config;

import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import com.java_dragons.dnd_tenebres.infrastructure.security.model.PlayerAuthenticationDetails;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(CurrentPlayerId.class) &&
                        parameter.getParameterType().equals(Long.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getDetails() instanceof PlayerAuthenticationDetails details) {
                    Long playerId = details.getPlayerId();
                    if (playerId == null) {
                        throw new IllegalStateException("Аккаунт не привязан к персонажу!");
                    }
                    return playerId;
                }
                throw new IllegalStateException("Доступ запрещен");
            }
        });
    }
}