package com.MoneyLog;

import com.MoneyLog.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ExpenseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception{
        this.token = signUpAndLogin("test@example.com", "홍길동", "password123");
        this.categoryId = createCategory("식비");
    }

    @Test
    void 지출_생성이_성공한다() throws Exception {
        ExpenseRequestDto request = new ExpenseRequestDto();
        request.setCategoryId(categoryId);
        request.setAmount(new BigDecimal("15000"));
        request.setContent("점심 식사");
        request.setMemo("동료와 함께");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("점심 식사"))
                .andExpect(jsonPath("$.amount").value(15000));
    }

    @Test
    void 지출_목록_조회가_성공한다() throws Exception {
        ExpenseRequestDto createRequest = new ExpenseRequestDto();
        createRequest.setCategoryId(categoryId);
        createRequest.setAmount(new BigDecimal("15000"));
        createRequest.setContent("점심 식사");
        createRequest.setMemo("동료와 함께");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("점심 식사"));
    }

    @Test
    void 지출_삭제가_성공한다() throws Exception {
        ExpenseRequestDto createRequest = new ExpenseRequestDto();
        createRequest.setCategoryId(categoryId);
        createRequest.setAmount(new BigDecimal("15000"));
        createRequest.setContent("점심 식사");
        createRequest.setMemo("동료와 함께");

        MvcResult createResult = mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long expenseId = objectMapper
                .readValue(createResult.getResponse().getContentAsString(), ExpenseResponseDto.class)
                .getId();

        mockMvc.perform(delete("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 다른_사용자의_지출_삭제는_403을_반환한다() throws Exception {
        ExpenseRequestDto createRequest = new ExpenseRequestDto();
        createRequest.setCategoryId(categoryId);
        createRequest.setAmount(new BigDecimal("15000"));
        createRequest.setContent("점심 식사");
        createRequest.setMemo("동료와 함께");

        MvcResult createResult = mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long expenseId = objectMapper
                .readValue(createResult.getResponse().getContentAsString(), ExpenseResponseDto.class)
                .getId();

        String otherUserToken = signUpAndLogin("other@example.com", "다른유저", "password123");

        mockMvc.perform(delete("/api/expenses/" + expenseId)
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

    private Long createCategory(String name) throws Exception {
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName(name);

        MvcResult result = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper
                .readValue(result.getResponse().getContentAsString(), CategoryResponseDto.class)
                .getId();
    }

    @Test
    void 지출_수정이_성공한다() throws Exception {
        ExpenseRequestDto createRequest = new ExpenseRequestDto();
        createRequest.setCategoryId(categoryId);
        createRequest.setAmount(new BigDecimal("15000"));
        createRequest.setContent("점심 식사");
        createRequest.setMemo("동료와 함께");

        MvcResult createResult = mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long expenseId = objectMapper
                .readValue(createResult.getResponse().getContentAsString(), ExpenseResponseDto.class)
                .getId();

        ExpenseRequestDto updateRequest = new ExpenseRequestDto();
        updateRequest.setCategoryId(categoryId); // 카테고리는 그대로 유지
        updateRequest.setAmount(new BigDecimal("20000"));
        updateRequest.setContent("저녁 식사");
        updateRequest.setMemo("가족과 함께");

        mockMvc.perform(put("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("저녁 식사"))
                .andExpect(jsonPath("$.amount").value(20000))
                .andExpect(jsonPath("$.memo").value("가족과 함께"));
    }

    @Test
    void 지출의_카테고리_변경이_성공한다() throws Exception {
        Long transportCategoryId = createCategory("교통비"); // 헬퍼 재사용

        ExpenseRequestDto createRequest = new ExpenseRequestDto();
        createRequest.setCategoryId(categoryId); // "식비"로 생성 (setUp에서 만든 categoryId)
        createRequest.setAmount(new BigDecimal("15000"));
        createRequest.setContent("점심 식사");
        createRequest.setMemo("동료와 함께");

        MvcResult createResult = mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long expenseId = objectMapper
                .readValue(createResult.getResponse().getContentAsString(), ExpenseResponseDto.class)
                .getId();

        ExpenseRequestDto updateRequest = new ExpenseRequestDto();
        updateRequest.setCategoryId(transportCategoryId); // 카테고리를 "교통비"로 변경
        updateRequest.setAmount(new BigDecimal("15000"));
        updateRequest.setContent("점심 식사");
        updateRequest.setMemo("동료와 함께");

        mockMvc.perform(put("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("교통비"));
    }

    private Long createExpense(Long categoryId, String content) throws Exception {
        ExpenseRequestDto request = new ExpenseRequestDto();
        request.setCategoryId(categoryId);
        request.setAmount(new BigDecimal("10000")); // 테스트에 중요하지 않으니 고정값
        request.setContent(content);
        request.setMemo("테스트 메모");

        MvcResult result = mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper
                .readValue(result.getResponse().getContentAsString(), ExpenseResponseDto.class)
                .getId();
    }

    @Test
    void category_필터가_정상_동작한다() throws Exception {
        Long foodCategoryId = categoryId; // setUp에서 만든 "식비" 재사용
        Long transportCategoryId = createCategory("교통비");

        createExpense(foodCategoryId, "점심"); // 헬퍼로 만들어두면 편함
        createExpense(transportCategoryId, "버스비");

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .param("categoryId", String.valueOf(foodCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("점심"));
    }
}
