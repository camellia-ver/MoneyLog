package com.MoneyLog;

import com.MoneyLog.dto.LoginRequestDto;
import com.MoneyLog.dto.LoginResponseDto;
import com.MoneyLog.dto.UserDto;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 회원가입_로그인_흐름이_정상_동작한다() throws Exception{
        UserDto signUpRequest = new UserDto();
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setUserName("홍길동");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());

        // 추가: loginRequest 선언 (회원가입 때와 동일한 email/password 사용)
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponseDto loginResponse = objectMapper.readValue(responseBody, LoginResponseDto.class);
        String token = loginResponse.getToken();

        // /api/some-protected-endpoint 블록 삭제!

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void 토큰_없이_보호된_API를_호출하면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized());
    }
}
