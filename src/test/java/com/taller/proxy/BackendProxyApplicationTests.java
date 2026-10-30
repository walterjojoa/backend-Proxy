package com.taller.proxy;

import com.taller.proxy.service.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Clock;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.ai.mock-delay-ms=0")
class BackendProxyApplicationTests {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	void generateReturnsTextAndUpdatesTheQuota() throws Exception {
		mockMvc.perform(generate("user-generate", "Explain the proxy pattern"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.text").isNotEmpty())
				.andExpect(jsonPath("$.totalTokens").isNumber());

		mockMvc.perform(get("/api/quota/status").header("X-User-Id", "user-generate"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.plan").value("FREE"))
				.andExpect(jsonPath("$.tokenLimit").value(50000))
				.andExpect(jsonPath("$.requestsThisMinute").value(1))
				.andExpect(jsonPath("$.requestsPerMinuteLimit").value(10));
	}

	@Test
	void eleventhRequestOfAFreeUserReturns429WithRetryAfter() throws Exception {
		for (int i = 0; i < 10; i++) {
			mockMvc.perform(generate("user-rate", "hello")).andExpect(status().isOk());
		}

		mockMvc.perform(generate("user-rate", "hello"))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().exists("Retry-After"))
				.andExpect(jsonPath("$.retryAfterSeconds").isNumber());
	}

	@Test
	void exhaustedQuotaReturns402() throws Exception {
		// Fill the quota with the account directly: prompts are limited to 4000 characters
		UserAccountRepository repository = context.getBean(UserAccountRepository.class);
		repository.getOrCreate("user-quota").consumeTokens(50_000, LocalDate.now(context.getBean(Clock.class)));

		mockMvc.perform(generate("user-quota", "hello"))
				.andExpect(status().isPaymentRequired())
				.andExpect(jsonPath("$.plan").value("FREE"));
	}

	@Test
	void upgradeChangesThePlanToPro() throws Exception {
		mockMvc.perform(post("/api/quota/upgrade").header("X-User-Id", "user-upgrade"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.plan").value("PRO"))
				.andExpect(jsonPath("$.tokenLimit").value(500000))
				.andExpect(jsonPath("$.requestsPerMinuteLimit").value(60));

		// Downgrades are not allowed
		mockMvc.perform(post("/api/quota/upgrade").header("X-User-Id", "user-upgrade")
						.contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"FREE\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void enterprisePlanShowsUnlimitedValuesAsNull() throws Exception {
		mockMvc.perform(post("/api/quota/upgrade").header("X-User-Id", "user-enterprise")
						.contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"ENTERPRISE\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.plan").value("ENTERPRISE"))
				.andExpect(jsonPath("$.tokenLimit").value(nullValue()))
				.andExpect(jsonPath("$.requestsPerMinuteLimit").value(nullValue()));
	}

	@Test
	void historyReturnsTheLastSevenDays() throws Exception {
		mockMvc.perform(get("/api/quota/history").header("X-User-Id", "user-history"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(7)));
	}

	@Test
	void missingUserHeaderReturns400() throws Exception {
		mockMvc.perform(get("/api/quota/status"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void emptyPromptReturns400() throws Exception {
		mockMvc.perform(generate("user-empty", " "))
				.andExpect(status().isBadRequest());
	}

	private static MockHttpServletRequestBuilder generate(String userId, String prompt) {
		return post("/api/ai/generate")
				.header("X-User-Id", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"prompt\":\"" + prompt + "\"}");
	}

}
