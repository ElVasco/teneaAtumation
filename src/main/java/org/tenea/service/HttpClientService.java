package org.tenea.service;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HttpClientService {

    @Value("${tenea.base.url}")
    private String baseUrl;

    @Value("${tenea.user.agent}")
    private String userAgent;

    public String extractVerificationToken(BasicCookieStore cookieStore) throws Exception {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build()) {
            HttpGet getForm = new HttpGet(baseUrl + "/GestionAccesos-3.2.4/ControlAccesos/CreateEmployee");
            getForm.setHeader("X-Requested-With", "XMLHttpRequest");
            String html = client.execute(getForm, r -> EntityUtils.toString(r.getEntity(), StandardCharsets.UTF_8));
            Matcher m = Pattern.compile("__RequestVerificationToken.*?value=\"([^\"]+)\"").matcher(html);
            if (m.find()) {
                return m.group(1);
            }
            throw new RuntimeException("Verification token not found");
        }
    }

    public void logTimeEntry(BasicCookieStore cookieStore, String verificationToken, String date, String startTime, String endTime, String locationCode) throws Exception {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build()) {
            String dt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss.SSS"));
            org.apache.hc.core5.net.URIBuilder ub = new org.apache.hc.core5.net.URIBuilder(baseUrl + "/GestionAccesos-3.2.4/ControlAccesos/InsertRegisterAccess")
                    .addParameter("punto_acceso", locationCode)
                    .addParameter("fecha_in", date)
                    .addParameter("hora_in", startTime)
                    .addParameter("fecha_out", date)
                    .addParameter("hora_out", endTime)
                    .addParameter("create_local_dt", dt);

            HttpGet req = new HttpGet(ub.build());
            req.setHeader("X-Requested-With", "XMLHttpRequest");
            req.setHeader("X-Request-Verification-Token", verificationToken);
            req.setHeader("Referer", baseUrl + "/GestionAccesos-3.2.4/ControlAccesos/CreateEmployee");

            client.execute(req, r -> {
                System.out.println("📅 " + date + " [" + startTime + "-" + endTime + "] -> Status: " + r.getCode());
                return null;
            });
            Thread.sleep(300);
        }
    }
}
