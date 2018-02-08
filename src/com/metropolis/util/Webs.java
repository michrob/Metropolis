package com.metropolis.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

public class Webs {

    public static String getWebPage(final URL url) throws IOException {
        String cacheKey = url.toString();
        Optional<String> cachedValue = Cache.getString(cacheKey);

        if (cachedValue.isPresent()) {
            return cachedValue.get();
        }

        URLConnection hc = url.openConnection();
        hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));

        StringBuilder response = new StringBuilder();

        String line;
        while ((line = in.readLine()) != null) {
            response.append(line).append("\n");
        }

        String pageResult = response.toString();

        Cache.putString(cacheKey, pageResult);

        return pageResult;
    }
}
