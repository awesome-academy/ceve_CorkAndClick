package com.sun.wineshop.configuration;

import com.nimbusds.jose.JOSEException;
import com.sun.wineshop.dto.request.VerifyTokenRequest;
import com.sun.wineshop.service.AuthenticationService;
import com.sun.wineshop.utils.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    private final SecurityProperties securityProperties;
    private final AuthenticationService authenticationService;
    private final MessageUtil messageUtil;

    private NimbusJwtDecoder nimbusJwtDecoder = null;
    private static final String ALGORITHM = "HS512";

    @Override
    public Jwt decode(String token) throws JwtException {

        try {
            var response = authenticationService.verifyToken(new VerifyTokenRequest(token));

            if (!response.isSuccess())
                throw new JwtException(messageUtil.getMessage("error.invalid.token"));
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(securityProperties.getJwt().getSignerKey().getBytes(), ALGORITHM);
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512).build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
