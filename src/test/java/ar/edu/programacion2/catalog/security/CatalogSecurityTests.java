package ar.edu.programacion2.catalog.security;

import java.time.Instant;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers=CatalogSecurityTests.Probe.class)
@Import({SecurityConfiguration.class,CatalogSecurityTests.Probe.class})
@TestPropertySource(properties="catalog.security.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class CatalogSecurityTests {
    @Autowired MockMvc mvc;
    @RestController static class Probe {
        @GetMapping("/api/catalog/categories") String categories() { return "[]"; }
        @GetMapping("/actuator/health") String health() { return "UP"; }
    }
    private Map<String,Object> claims() {
        Instant now=Instant.now();
        var result=new HashMap<String,Object>();
        result.put("sub","11111111-1111-1111-1111-111111111111"); result.put("iss","turnos-booking");
        result.put("aud",List.of("turnos-backends")); result.put("iat",now); result.put("exp",now.plusSeconds(600));
        result.put("auth","ROLE_USER"); return result;
    }
    private String token(Map<String,Object> claims, byte[] key, MacAlgorithm algorithm) {
        var encoder=NimbusJwtEncoder.withSecretKey(new SecretKeySpec(key,"HmacSHA256")).algorithm(algorithm).build();
        return encoder.encode(JwtEncoderParameters.from(JwtClaimsSet.builder().claims(map -> map.putAll(claims)).build())).getTokenValue();
    }
    private String token(Map<String,Object> claims) { return token(claims,new byte[32],MacAlgorithm.HS256); }
    @Test void acceptsBookingTokenAndKeepsHealthPublicWithoutSession() throws Exception {
        mvc.perform(get("/api/catalog/categories").header("Authorization","Bearer "+token(claims())))
            .andExpect(status().isOk()).andExpect(content().string("[]"))
            .andExpect(header().doesNotExist("Set-Cookie")).andExpect(header().string("Cache-Control",org.hamcrest.Matchers.containsString("no-store")));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
    @ParameterizedTest @ValueSource(strings={"/api/catalog/categories","/api/catalog/professionals","/api/catalog/professionals/1/schedules","/api/catalog/synchronization/status","/actuator/env"})
    void requiresAuthentication(String path) throws Exception { mvc.perform(get(path)).andExpect(status().isUnauthorized()); }
    @ParameterizedTest @ValueSource(strings={"sub","iss","aud","iat","exp"})
    void requiresAllIdentityAndTimeClaims(String field) throws Exception {
        var values=claims(); values.remove(field);
        mvc.perform(get("/api/catalog/categories").header("Authorization","Bearer "+token(values))).andExpect(status().isUnauthorized());
    }
    @ParameterizedTest @ValueSource(strings={"expired","future-issued","future-not-before","wrong-issuer","wrong-audience","invalid-subject","noncanonical-subject","expires-before-issued","auth-array"})
    void rejectsInvalidClaims(String variant) throws Exception {
        var values=claims();
        switch(variant) {
            case "expired" -> {values.put("iat",Instant.now().minusSeconds(600)); values.put("exp",Instant.now().minusSeconds(1));}
            case "future-issued" -> values.put("iat",Instant.now().plusSeconds(60));
            case "future-not-before" -> values.put("nbf",Instant.now().plusSeconds(60));
            case "wrong-issuer" -> values.put("iss","catedra");
            case "wrong-audience" -> values.put("aud",List.of("other-service"));
            case "invalid-subject" -> values.put("sub","patient-name");
            case "noncanonical-subject" -> values.put("sub","1-1-1-1-1");
            case "expires-before-issued" -> {values.put("iat",Instant.now()); values.put("exp",Instant.now().minusSeconds(10));}
            case "auth-array" -> values.put("auth",List.of("ROLE_USER"));
            default -> throw new IllegalArgumentException();
        }
        String encoded = variant.equals("expires-before-issued") ? invalidChronologyToken() : token(values);
        mvc.perform(get("/api/catalog/categories").header("Authorization","Bearer "+encoded)).andExpect(status().isUnauthorized());
    }
    @ParameterizedTest @ValueSource(strings={"","ROLE_ADMIN","USER","ROLE_USER_OTHER"})
    void requiresExactUserPermission(String permission) throws Exception {
        var values=claims(); values.put("auth",permission);
        mvc.perform(get("/api/catalog/categories").header("Authorization","Bearer "+token(values))).andExpect(status().isForbidden());
    }
    @Test void rejectsMissingRoleAndDoesNotEnableWritesOrOtherEndpoints() throws Exception {
        var values=claims(); values.remove("auth");
        mvc.perform(get("/api/catalog/categories").header("Authorization","Bearer "+token(values))).andExpect(status().isForbidden());
        String bearer="Bearer "+token(claims());
        mvc.perform(post("/api/catalog/categories").header("Authorization",bearer)).andExpect(status().isForbidden());
        mvc.perform(get("/actuator/env").header("Authorization",bearer)).andExpect(status().isForbidden());
        mvc.perform(get("/api/unlisted").header("Authorization",bearer)).andExpect(status().isForbidden());
    }
    @Test void rejectsWrongSignatureAlgorithmMalformedAndQueryTokens() throws Exception {
        byte[] wrong=new byte[32]; Arrays.fill(wrong,(byte)1);
        for(String value:List.of(token(claims(),wrong,MacAlgorithm.HS256),token(claims(),new byte[64],MacAlgorithm.HS512),"not-a-jwt","eyJhbGciOiJub25lIn0.e30.")) {
            mvc.perform(get("/api/catalog/categories").header("Authorization","Bearer "+value)).andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/catalog/categories").param("access_token",token(claims()))).andExpect(status().isUnauthorized());
    }

    private String invalidChronologyToken() throws Exception {
        var claims=new com.nimbusds.jwt.JWTClaimsSet.Builder().subject("11111111-1111-1111-1111-111111111111")
            .issuer("turnos-booking").audience("turnos-backends").claim("auth","ROLE_USER")
            .issueTime(Date.from(Instant.now())).expirationTime(Date.from(Instant.now().minusSeconds(10))).build();
        var signed=new com.nimbusds.jwt.SignedJWT(new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS256),claims);
        signed.sign(new com.nimbusds.jose.crypto.MACSigner(new byte[32]));
        return signed.serialize();
    }
}
