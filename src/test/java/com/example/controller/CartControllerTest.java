package com.example.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.interceptor.CartCookieTouchInterceptor;
import com.example.security.JwtAuthFilter;
import com.example.service.CartService;
import com.example.util.CookieUtil;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CartController.class)
@Import({CookieUtil.class, CartControllerTest.TestConfig.class})
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CartService cartService;

    @MockitoBean
    JwtAuthFilter jwtAuthFilter;

    @Autowired
    CookieUtil cookieUtil;

    @Nested
    class ShowCart {
        @Nested
        class Guest {
            Cookie cookie;

            @BeforeEach
            void setup() {
                cookie = new Cookie("cartId", "id");
            }

            @Test
            void showCart_absent() throws Exception {
                cookie = new Cookie("invalid", "id");

                mockMvc.perform(get("/cart").cookie(cookie))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items").isEmpty())
                        .andExpect(jsonPath("$.totalQty").value(0))
                        .andExpect(jsonPath("$.totalPriceIncl").value(0));
            }

            @Test
            void showCart_valid() throws Exception {
                CartDto dto = new CartDto(List.of(new CartItemDto()));
                doReturn(dto).when(cartService).showCart(anyString());

                MvcResult result = mockMvc.perform(get("/cart").cookie(cookie))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items", hasSize(1)))
                        .andExpect(jsonPath("$.totalQty").value(2))
                        .andExpect(jsonPath("$.totalPriceIncl").value(5000))
                        .andReturn();

                assertThat(result.getResponse().getHeader("Set-Cookie")).isNull();
            }

            @Test
            void showCart_expired() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.CONFLICT))
                        .when(cartService).showCart(anyString());

                mockMvc.perform(get("/cart").cookie(cookie))
                        .andExpect(status().isOk())
                        .andExpect(header().string(CartController.HEADER_CART_EVENT,
                                CartController.EVENT_CART_EXPIRED))
                        .andExpect(jsonPath("$.items").isEmpty())
                        .andExpect(jsonPath("$.totalQty").value(0))
                        .andExpect(jsonPath("$.totalPriceIncl").value(0));
            }
        }

        @Nested
        class User {

            @BeforeEach
            void setup() {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("user", "N/A"));
            }

            @AfterEach
            void tearDown() {
                SecurityContextHolder.clearContext();
            }

            @Test
            void showCart_absent() throws Exception {
                doReturn(Optional.empty()).when(cartService).findUserCartId(anyString());

                mockMvc.perform(get("/cart"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items").isEmpty())
                        .andExpect(jsonPath("$.totalQty").value(0))
                        .andExpect(jsonPath("$.totalPriceIncl").value(0));
            }

            @Test
            void showCart_valid() throws Exception {
                doReturn(Optional.of("id")).when(cartService).findUserCartId(anyString());

                CartDto dto = new CartDto(List.of());
                doReturn(dto).when(cartService).showCart(anyString());

                mockMvc.perform(get("/cart"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items").isEmpty())
                        .andExpect(jsonPath("$.totalQty").value(2))
                        .andExpect(jsonPath("$.totalPriceIncl").value(5000));
            }

            @Test
            void showCart_expired() throws Exception {
                doReturn(Optional.of("id")).when(cartService).findUserCartId(anyString());
                doThrow(new ResponseStatusException(HttpStatus.CONFLICT))
                        .when(cartService).showCart(anyString());

                mockMvc.perform(get("/cart"))
                        .andExpect(status().isOk())
                        .andExpect(header().string(CartController.HEADER_CART_EVENT,
                                CartController.EVENT_CART_EXPIRED))
                        .andExpect(jsonPath("$.items").isEmpty())
                        .andExpect(jsonPath("$.totalQty").value(0))
                        .andExpect(jsonPath("$.totalPriceIncl").value(0));
            }
        }
    }

    @Nested
    class AddtoCart {
        String productId = "97113c2c-719a-490c-9979-144d92905c33";

        @Nested
        class Guest {
            Cookie cookie;

            @BeforeEach
            void setup() {
                cookie = new Cookie("cartId", "id");
            }

            @Test
            void addtoCart_absent() throws Exception {
                cookie = new Cookie("invalid", "id");
                doReturn(Optional.of("id")).when(cartService).addToCart(any(),
                        any(),
                        anyString(),
                        any());

                MvcResult result = mockMvc.perform(post("/cart/items/{productId}", productId) // PUT→POST
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "qty": 3
                                }
                                """))
                        .andExpect(status().isOk())
                        .andReturn();
                Cookie resCookie = result.getResponse().getCookie("cartId");
                assertThat(result.getResponse().getHeader("Set-Cookie")).isNotNull();

                assertThat(resCookie).isNotNull();
                assertThat(resCookie.getValue()).isEqualTo("id");
            }

            @Test
            void addtoCart_valid() throws Exception {
                doReturn(Optional.empty()).when(cartService).addToCart(any(),
                        any(),
                        anyString(),
                        any());

                MvcResult result = mockMvc.perform(post("/cart/items/{productId}", productId) // PUT→POST
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "qty": 3
                                }
                                """))
                        .andExpect(status().isOk())
                        .andReturn();

                Cookie resCookie = result.getResponse().getCookie("cartId");
                assertThat(result.getResponse().getHeader("Set-Cookie")).isNotNull();

                assertThat(resCookie).isNotNull();
                assertThat(resCookie.getValue()).isEqualTo("id");
                assertThat(resCookie.getMaxAge()).isEqualTo(60 * 60 * 24 * 14);
            }
        }

        @Nested
        class User {

            @BeforeEach
            void setup() {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("user", "N/A"));
            }

            @AfterEach
            void tearDown() {
                SecurityContextHolder.clearContext();
            }

            @Test
            void addtoCart_absent() throws Exception {
                doReturn(Optional.of("id")).when(cartService).addToCart(any(),
                        any(),
                        anyString(),
                        any());

                MvcResult result = mockMvc.perform(post("/cart/items/{productId}", productId) // PUT→POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "qty": 3
                                }
                                """))
                        .andExpect(status().isOk())
                        .andReturn();

                assertThat(result.getResponse().getHeader("Set-Cookie")).isNull();
            }

            @Test
            void addtoCart_valid() throws Exception {
                doReturn(Optional.empty()).when(cartService).addToCart(any(),
                        any(),
                        anyString(),
                        any());

                MvcResult result = mockMvc.perform(post("/cart/items/{productId}", productId) // PUT→POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "qty": 3
                                }
                                """))
                        .andExpect(status().isOk())
                        .andReturn();

                assertThat(result.getResponse().getHeader("Set-Cookie")).isNull();
            }

            @Test
            void addToCart_expired() throws Exception {
                String newId = "00000000-0000-0000-0000-000000000001";
                UUID u = UUID.fromString(newId);
                doReturn(Optional.of(newId))
                        .when(cartService).addToCart(any(), any(), anyString(), any());

                try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
                    mocked.when(UUID::randomUUID).thenReturn(u);

                    MvcResult result = mockMvc.perform(post("/cart/items/{productId}", productId) // PUT→POST
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "qty": 3
                                    }
                                    """))
                            .andExpect(status().isOk())
                            .andExpect(header().string(CartController.HEADER_CART_EVENT,
                                    CartController.EVENT_CART_EXPIRED))
                            .andReturn();

                    assertThat(result.getResponse().getHeader("Set-Cookie")).isNull();
                }
            }
        }
    }

    @Nested
    class ChangeQty {
        String productId = "97113c2c-719a-490c-9979-144d92905c33";

        @Nested
        class Guest {
            Cookie cookie;

            @BeforeEach
            void setup() {
                cookie = new Cookie("cartId", "id");
            }

            @Test
            void changeQty_valid() throws Exception {
                mockMvc.perform(put("/cart/items/{productId}/quantity", productId) // PATCH→PUT
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("5"))
                        .andExpect(status().isOk());

                verify(cartService).changeQty("id", null, productId, 5);
            }

            @Test
            void changeQty_absent() throws Exception {
                cookie = new Cookie("invalid", "id");

                mockMvc.perform(put("/cart/items/{productId}/quantity", productId) // PATCH→PUT
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("3"))
                        .andExpect(status().isOk());

                verify(cartService).changeQty(null, null, productId, 3);
            }

            @Test
            void changeQty_invalidQuantityTooLow() throws Exception {
                mockMvc.perform(put("/cart/items/{productId}/quantity", productId) // PATCH→PUT
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("0"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void changeQty_invalidQuantityTooHigh() throws Exception {
                mockMvc.perform(put("/cart/items/{productId}/quantity", productId) // PATCH→PUT
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("21"))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        class User {

            @BeforeEach
            void setup() {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("user", "N/A"));
            }

            @AfterEach
            void tearDown() {
                SecurityContextHolder.clearContext();
            }

            @Test
            void changeQty_valid() throws Exception {
                doReturn(Optional.of("cartId")).when(cartService).findUserCartId("user");

                mockMvc.perform(put("/cart/items/{productId}/quantity", productId) // PATCH→PUT
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("3"))
                        .andExpect(status().isOk());

                verify(cartService).changeQty("cartId", "user", productId, 3);
            }

            @Test
            void changeQty_absent() throws Exception {
                doReturn(Optional.empty()).when(cartService).findUserCartId("user");

                MvcResult result = mockMvc.perform(put("/cart/items/{productId}/quantity", productId) // PATCH→PUT
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("2"))
                        .andExpect(status().isOk())
                        .andReturn();

                verify(cartService).changeQty(null, "user", productId, 2);

                assertThat(result.getResponse().getHeader("Set-Cookie")).isNull();
            }
        }
    }

    @Nested
    class RemoveFromCart {
        String productId = "97113c2c-719a-490c-9979-144d92905c33";

        @Nested
        class Guest {
            Cookie cookie;

            @BeforeEach
            void setup() {
                cookie = new Cookie("cartId", "id");
                reset(cartService);
            }

            @Test
            void removeFromCart_valid() throws Exception {
                MvcResult result = mockMvc.perform(delete("/cart/items/{productId}", productId)
                        .cookie(cookie))
                        .andExpect(status().isOk())
                        .andReturn();

                verify(cartService).removeItem("id", productId);

                Cookie resCookie = result.getResponse().getCookie("cartId");
                assertThat(result.getResponse().getHeader("Set-Cookie")).isNotNull();

                assertThat(resCookie).isNotNull();
                assertThat(resCookie.getValue()).isEqualTo("id");
                assertThat(resCookie.getMaxAge()).isEqualTo(60 * 60 * 24 * 14);
            }

            @Test
            void removeFromCart_absent() throws Exception {
                cookie = new Cookie("invalid", "id");

                mockMvc.perform(delete("/cart/items/{productId}", productId)
                        .cookie(cookie))
                        .andExpect(status().isOk());

                verify(cartService).removeItem(null, productId);
            }
        }

        @Nested
        class User {

            @BeforeEach
            void setup() {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("user", "N/A"));
            }

            @AfterEach
            void tearDown() {
                SecurityContextHolder.clearContext();
            }

            @Test
            void removeFromCart_valid() throws Exception {
                doReturn(Optional.of("cartId")).when(cartService).findUserCartId("user");

                mockMvc.perform(delete("/cart/items/{productId}", productId))
                        .andExpect(status().isOk());

                verify(cartService).removeItem("cartId", productId);
            }

            @Test
            void removeFromCart_absent() throws Exception {
                doReturn(Optional.empty()).when(cartService).findUserCartId("user");

                mockMvc.perform(delete("/cart/items/{productId}", productId))
                        .andExpect(status().isOk());

                verify(cartService).removeItem(null, productId);
            }
        }
    }

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Autowired
        CookieUtil cookieUtil;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new CartCookieTouchInterceptor(cookieUtil));
        }
    }
}
