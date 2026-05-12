package de.kaviedes.thinkofyou3.config;

import de.kaviedes.thinkofyou3.security.JwtFilter;
import de.kaviedes.thinkofyou3.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityConfigTest.TestController.class)
@Import({SecurityConfig.class, SecurityConfigTest.TestSecurityBeans.class})
class SecurityConfigTest {
    private final MockMvc mockMvc;

    @Autowired
    SecurityConfigTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void legalStaticPages_arePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/privacy-policy.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Privacy Policy for Thinking of You")));

        mockMvc.perform(get("/account-deletion.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thinking of You Account Deletion")));
    }

    @Test
    void otherRoutes_stillRequireAuthentication() throws Exception {
        mockMvc.perform(get("/private"))
                .andExpect(status().isForbidden());
    }

    @Controller
    static class TestController {
        @GetMapping("/privacy-policy.html")
        @ResponseBody
        String privacyPolicy() {
            return "privacy";
        }

        @GetMapping("/account-deletion.html")
        @ResponseBody
        String accountDeletion() {
            return "deletion";
        }

        @GetMapping("/private")
        @ResponseBody
        String privateRoute() {
            return "private";
        }
    }

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        JwtFilter jwtFilter() {
            return new JwtFilter(Mockito.mock(JwtUtil.class)) {
                @Override
                protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain
                ) throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }
}
