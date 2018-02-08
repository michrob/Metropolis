package com.metropolis.stocks.source;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

@Slf4j
public class Robinhood {

    private static final String QUOTE_KEY = "ask_price";

    private final ObjectMapper objectMapper = new ObjectMapper();
    HashMap<String, String> endpoints = new HashMap<String, String>() {{
        put("login", "https://api.robinhood.com/api-token-auth/");
        put("investment_profile", "https://api.robinhood.com/user/investment_profile/");
        put("accounts", "https://api.robinhood.com/accounts/");
        put("ach_iav_auth", "https://api.robinhood.com/ach/iav/auth/");
        put("ach_relationships", "https://api.robinhood.com/ach/relationships/");
        put("ach_transfers", "https://api.robinhood.com/ach/transfers/");
        put("applications", "https://api.robinhood.com/applications/");
        put("dividends", "https://api.robinhood.com/dividends/");
        put("edocuments", "https://api.robinhood.com/documents/");
        put("instruments", "https://api.robinhood.com/instruments/");
        put("margin_upgrades", "https://api.robinhood.com/margin/upgrades/");
        put("markets", "https://api.robinhood.com/markets/");
        put("notifications", "https://api.robinhood.com/notifications/");
        put("orders", "https://api.robinhood.com/orders/");
        put("password_reset", "https://api.robinhood.com/password_reset/request/");
        put("quotes", "https://api.robinhood.com/quotes/");
        put("document_requests", "https://api.robinhood.com/upload/document_requests/");
        put("user", "https://api.robinhood.com/user/");
        put("watchlists", "https://api.robinhood.com/watchlists/");
    }};
    private List<Header> headers = ImmutableList.of(new BasicHeader("Accept", "*/*"),
                                                    new BasicHeader("Accept-Encoding", "gzip, deflate"),
                                                    new BasicHeader("Accept-Language", "en;q=1, fr;q=0.9, de;q=0.8, ja;q=0.7, nl;q=0.6, it;q=0.5"),
                                                    new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                                                    new BasicHeader("X-Robinhood-API-Version", "1.70.0"),
                                                    new BasicHeader("Connection", "keep-alive"),
                                                    new BasicHeader("User-Agent", "Robinhood/823 (iPhone; iOS 7.1.2; Scale/2.00)"));
    private final HttpClient httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
    private String username;
    private String password;
    private String auth_token;

    public Robinhood(final String username, final String password) {
        this.username = username;
        this.password = password;
        this.doLogin();
    }

    private Optional<String> executeRequest(final HttpUriRequest httpPost) {
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String contents = IOUtils.toString(entity.getContent(), Charset.defaultCharset());
            return Optional.of(contents);
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

        return Optional.empty();
    }

    private void doLogin() {
        HttpPost httpPost = new HttpPost(endpoints.get("login"));
        List<NameValuePair> params = ImmutableList.of(new BasicNameValuePair("username", username),
                                                      new BasicNameValuePair("password", password));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.warn(e.getMessage());
        }

        Optional<String> jsonResult = executeRequest(httpPost);

        if (!jsonResult.isPresent()) {
            return;
        }

        try {
            Map<String, String> map = objectMapper.readValue(jsonResult.get(), new TypeReference<Map<String, Object>>() {
            });
            this.auth_token = map.get("token");
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }

    public Map<String, Float> getQuotes(final Set<String> symbols) {

        List<NameValuePair> params = ImmutableList.of(new BasicNameValuePair("trackedSymbols", Joiner.on(',').join(symbols)));
        String paramString = URLEncodedUtils.format(params, "utf-8");

        HttpGet httpGet = new HttpGet(endpoints.get("quotes") + "?" + paramString);

        Optional<String> jsonResult = executeRequest(httpGet);

        if (!jsonResult.isPresent()) {
            return Collections.emptyMap();
        }

        Map<String, Float> symbolToQuote = new HashMap<>();

        try {
            Map<String, List<Map<String, String>>> map =
                    objectMapper.readValue(jsonResult.get(), new TypeReference<Map<String, List<Map<String, String>>>>() {
                    });
            for (Map<String, String> quoteMap : map.get("results")) {
                symbolToQuote.put(quoteMap.get("symbol"), Float.valueOf(quoteMap.get(QUOTE_KEY)));
            }
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

        return symbolToQuote;
    }

    public Optional<Float> getQuote(final String symbol) {

        List<NameValuePair> params = ImmutableList.of(new BasicNameValuePair("trackedSymbols", symbol));
        String paramString = URLEncodedUtils.format(params, "utf-8");

        HttpGet httpGet = new HttpGet(endpoints.get("quotes") + "?" + paramString);

        Optional<String> jsonResult = executeRequest(httpGet);

        if (!jsonResult.isPresent()) {
            return Optional.empty();
        }

        try {
            Map<String, List<Map<String, String>>> map =
                    objectMapper.readValue(jsonResult.get(), new TypeReference<Map<String, List<Map<String, String>>>>() {});
            float last_price = Float.valueOf(map.get("results").get(0).get(QUOTE_KEY));
            return Optional.of(last_price);
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

        return Optional.empty();
    }

}
