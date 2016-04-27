package io.pivotal.security.controller;

import io.pivotal.security.entity.Secret;
import io.pivotal.security.repository.SecretRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.RequestBuilder;

import java.io.IOException;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SecretsControllerTest extends HtmlUnitTestBase {

    @Autowired
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private SecretRepository secretRepository;

    @Test
    @DirtiesContext
    public void validPutSecret() throws Exception {
        HashMap<String, String> values = new HashMap<>();
        values.put("key1", "value1");
        Secret secret = new Secret(values);

        String secretJson = json(secret);

        RequestBuilder requestBuilder = putRequestBuilder("/api/secret/testid", secretJson);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(secretJson));

        Assert.assertEquals(secretRepository.get("testid"), secret);
    }

    @Test
    @DirtiesContext
    public void validGetSecret() throws Exception {
        HashMap<String, String> values = new HashMap<>();
        values.put("key1", "value1");
        Secret secret = new Secret(values);

        secretRepository.set("whatever", secret);

        String expectedJson = json(secret);

        mockMvc.perform(get("/api/secret/whatever"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(expectedJson));
    }

    @Test
    @DirtiesContext
    public void validDeleteSecret() throws Exception {
        HashMap<String, String> values = new HashMap<>();
        values.put("key1", "value1");
        Secret secret = new Secret(values);

        secretRepository.set("whatever", secret);

        mockMvc.perform(delete("/api/secret/whatever"))
                .andExpect(status().isOk());

        Assert.assertNull(secretRepository.get("whatever"));
    }

    @Test
    public void getReturnsNotFoundWhenSecretDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/secret/whatever"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteReturnsNotFoundWhenSecretDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/secret/whatever"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void invalidPutWithEmptyJSONShouldReturnBadRequest() throws Exception {
        String badResponse = "{\"error\": \"The request could not be fulfilled because the request path or body did not meet expectation. Please check the documentation for required formatting and retry your request.\"}";

        RequestBuilder requestBuilder = putRequestBuilder("/api/secret/testid", "{}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(badResponse));
    }

    @Test
    public void invalidPutWithNoBodyShouldReturnBadRequest() throws Exception {
        String badResponse = "{\"error\": \"The request could not be fulfilled because the request path or body did not meet expectation. Please check the documentation for required formatting and retry your request.\"}";

        RequestBuilder requestBuilder = putRequestBuilder("/api/secret/testid", "");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(badResponse));
    }

    private RequestBuilder putRequestBuilder(String path, String requestBody) {
        return put(path)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON_UTF8);
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}