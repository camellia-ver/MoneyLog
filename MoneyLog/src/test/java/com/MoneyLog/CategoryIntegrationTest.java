package com.MoneyLog;

import com.MoneyLog.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CategoryIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception{
        this.token = signUpAndLogin("test@example.com", "홍길동", "password123");
    }

    @Test
    void 카테고리_생성이_성공한다() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName("식비");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("식비"));
    }

    @Test
    void 카테고리_목록_조회가_성공한다() throws Exception {
        CategoryRequestDto createRequest = new CategoryRequestDto();
        createRequest.setName("식비");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("식비"));
    }

    @Test
    void 카테고리_삭제가_성공한다() throws Exception {
        // Given — 카테고리 생성 후 id 꺼내기
        CategoryRequestDto createRequest = new CategoryRequestDto();
        createRequest.setName("식비");

        MvcResult createResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        CategoryResponseDto createdCategory =
                objectMapper.readValue(responseBody, CategoryResponseDto.class);
        Long categoryId = createdCategory.getId();

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 다른_사용자의_카테고리_삭제는_403을_반환한다() throws Exception {
        // Given — A(token)가 카테고리 생성
        CategoryRequestDto createRequest = new CategoryRequestDto();
        createRequest.setName("식비");

        MvcResult createResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = objectMapper
                .readValue(createResult.getResponse().getContentAsString(), CategoryResponseDto.class)
                .getId();

        String otherUserToken = signUpAndLogin("other@example.com", "다른유저", "password123");

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    private String signUpAndLogin(String email, String userName, String password) throws Exception {
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setEmail(email);
        signUpRequest.setUserName(userName);
        signUpRequest.setPassword(password);

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponseDto loginResponse = objectMapper.readValue(responseBody, LoginResponseDto.class);
        return loginResponse.getToken();
    }

    @Test
    void 카테고리_수정이_성공한다() throws Exception {
        CategoryRequestDto createRequest = new CategoryRequestDto();
        createRequest.setName("식비");

        MvcResult createResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = objectMapper
                .readValue(createResult.getResponse().getContentAsString(), CategoryResponseDto.class)
                .getId();

        CategoryRequestDto updateRequest = new CategoryRequestDto();
        updateRequest.setName("외식비");

        mockMvc.perform(put("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("외식비"));
    }

    @Test
    void 자기_자신과_같은_이름으로_수정하면_성공한다() throws Exception {
        CategoryRequestDto createRequest = new CategoryRequestDto();
        createRequest.setName("식비");

        MvcResult createResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = objectMapper
                .readValue(createResult.getResponse().getContentAsString(), CategoryResponseDto.class)
                .getId();

        CategoryRequestDto updateRequest = new CategoryRequestDto();
        updateRequest.setName("식비"); // 그대로 같은 이름

        mockMvc.perform(put("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk()); // 실패하면 안 됨!
    }

    @Test
    void 다른_카테고리와_이름이_중복되면_수정이_실패한다() throws Exception {
        CategoryRequestDto request1 = new CategoryRequestDto();
        request1.setName("식비");
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        CategoryRequestDto request2 = new CategoryRequestDto();
        request2.setName("교통비");
        MvcResult createResult2 = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andReturn();

        Long category2Id = objectMapper
                .readValue(createResult2.getResponse().getContentAsString(), CategoryResponseDto.class)
                .getId();

        CategoryRequestDto updateRequest = new CategoryRequestDto();
        updateRequest.setName("식비"); // 이미 존재하는 이름으로 변경 시도

        mockMvc.perform(put("/api/categories/" + category2Id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }
}
