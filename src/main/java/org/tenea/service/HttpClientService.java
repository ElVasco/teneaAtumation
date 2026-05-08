package org.tenea.service;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HttpClientService {
    private static final Logger logger = LogManager.getLogger(HttpClientService.class);

    // ...existing code...

    @Value("${tenea.base.url}")
    private String baseUrl;

    @Value("${tenea.app.path}")
    private String appPath;

    @Value("${tenea.create.employee.path}")
    private String createEmployeePath;

    @Value("${tenea.insert.register.path}")
    private String insertRegisterPath;

    @Value("${tenea.update.register.path}")
    private String updateRegisterPath;

    @Value("${tenea.list.employee.path}")
    private String listEmployeePath;

    @Value("${tenea.list.employee.referer}")
    private String listEmployeeReferer;

    @Value("${tenea.user.agent}")
    private String userAgent;

    public String extractVerificationToken(BasicCookieStore cookieStore) throws Exception {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build()) {
            HttpGet getForm = new HttpGet(baseUrl + "/" + appPath + createEmployeePath);
            getForm.setHeader("X-Requested-With", "XMLHttpRequest");
            String html = client.execute(getForm, r -> EntityUtils.toString(r.getEntity(), StandardCharsets.UTF_8));
            Matcher m = Pattern.compile("__RequestVerificationToken.*?value=\"([^\"]+)\"").matcher(html);
            if (m.find()) {
                return m.group(1);
            }
            throw new RuntimeException("Verification token not found");
        }
    }

    public int logTimeEntry(BasicCookieStore cookieStore, String verificationToken, String date, String startTime, String endTime, String locationCode) throws Exception {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build()) {
            String dt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss.SSS"));
            org.apache.hc.core5.net.URIBuilder ub = new org.apache.hc.core5.net.URIBuilder(baseUrl + "/" + appPath + insertRegisterPath)
                    .addParameter("punto_acceso", locationCode)
                    .addParameter("fecha_in", date)
                    .addParameter("hora_in", startTime)
                    .addParameter("fecha_out", date)
                    .addParameter("hora_out", endTime)
                    .addParameter("create_local_dt", dt);

            HttpGet req = new HttpGet(ub.build());
            req.setHeader("X-Requested-With", "XMLHttpRequest");
            req.setHeader("X-Request-Verification-Token", verificationToken);
            req.setHeader("Referer", baseUrl + "/" + appPath + createEmployeePath);

            int statusCode = client.execute(req, r -> {
                logger.info("📅 " + date + " [" + startTime + "-" + endTime + "] -> Status: " + r.getCode());
                String responseBody = r.getEntity() != null ? EntityUtils.toString(r.getEntity(), StandardCharsets.UTF_8) : "";
                if (r.getCode() < 200 || r.getCode() >= 300) {
                    throw new RuntimeException("Tenea rejected time entry. Status: " + r.getCode() + " Body: " + responseBody);
                }
                return r.getCode();
            });
            Thread.sleep(300);
            return statusCode;
        }
    }

    public int updateTimeEntry(BasicCookieStore cookieStore, String verificationToken, String id, String idRegistroEntrada,
                               String idRegistroSalida, String startDate, String startTime, String endDate, String endTime,
                               String locationCode, String observaciones, String ip) throws Exception {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build()) {
            String dt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss.SSS"));
            org.apache.hc.core5.net.URIBuilder ub = new org.apache.hc.core5.net.URIBuilder(baseUrl + "/" + appPath + updateRegisterPath)
                    .addParameter("id", id)
                    .addParameter("id_registro_entrada", idRegistroEntrada)
                    .addParameter("id_registro_salida", idRegistroSalida)
                    .addParameter("punto_acceso", locationCode)
                    .addParameter("fecha_in", startDate)
                    .addParameter("hora_in", startTime)
                    .addParameter("fecha_out", endDate)
                    .addParameter("hora_out", endTime)
                    .addParameter("observaciones", observaciones != null ? observaciones : "")
                    .addParameter("create_local_dt", dt)
                    .addParameter("ip", ip != null ? ip : "")
                    .addParameter("_", String.valueOf(System.currentTimeMillis()));

            HttpGet req = new HttpGet(ub.build());
            req.setHeader("X-Requested-With", "XMLHttpRequest");
            req.setHeader("X-Request-Verification-Token", verificationToken);
            req.setHeader("Referer", baseUrl + "/" + appPath + createEmployeePath);

            int statusCode = client.execute(req, r -> {
                logger.info("Update " + startDate + " [" + startTime + "-" + endTime + "] -> Status: " + r.getCode());
                String responseBody = r.getEntity() != null ? EntityUtils.toString(r.getEntity(), StandardCharsets.UTF_8) : "";
                if (r.getCode() < 200 || r.getCode() >= 300) {
                    throw new RuntimeException("Tenea rejected time entry update. Status: " + r.getCode() + " Body: " + responseBody);
                }
                return r.getCode();
            });
            Thread.sleep(300);
            return statusCode;
        }
    }

    public List<org.tenea.dto.TimeEntryRecord> getTimeEntryList(BasicCookieStore cookieStore, String verificationToken, String fechaDesde, String fechaHasta) throws Exception {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build()) {

            // Construir el valor de data con doble encoding (pre-encodear caracteres especiales)
            String dataValue = "filter_f_fecha_dt_desde=" + fechaDesde.replace("/", "%2F") +
                    "&filter_f_fecha_dt_hasta=" + fechaHasta.replace("/", "%2F") +
                    "&filter_f_hora_desde=00%3A00" +
                    "&filter_f_hora_hasta=23%3A59" +
                    "&ddl_Oficina=" +
                    "&filter_entrada=on" +
                    "&filter_salida=on" +
                    "&filter_acceso_correcto=on" +
                    "&filter_acceso_incorrecto=on" +
                    "&order_fecha=on" +
                    "&Order=1" +
                    "&OrderZ=2" +
                    "&OrderE=2" +
                    "&__RequestVerificationToken=" + verificationToken;

            // Construir parámetros del formulario de manera estructurada
            List<org.apache.hc.core5.http.NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("data", dataValue));

            HttpPost req = new HttpPost(baseUrl + "/" + appPath + listEmployeePath);
            req.setHeader("accept", "text/html, */*; q=0.01");
            req.setHeader("accept-language", "es-ES,es;q=0.9");
            req.setHeader("X-Requested-With", "XMLHttpRequest");
            req.setHeader("Referer", baseUrl + "/" + appPath + listEmployeeReferer);

            // Usar UrlEncodedFormEntity para encoding automático correcto
            req.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            String html = client.execute(req, r -> {
                String responseBody = r.getEntity() != null ? EntityUtils.toString(r.getEntity(), StandardCharsets.UTF_8) : "";
                if (r.getCode() < 200 || r.getCode() >= 300) {
                    throw new RuntimeException("Tenea rejected time entry list request. Status: " + r.getCode() + " Body: " + responseBody);
                }
                return responseBody;
            });

            Document doc = Jsoup.parse(html);
            List<org.tenea.dto.TimeEntryRecord> listado = new ArrayList<>();

            // Selector corregido: Buscamos filas <tr> que tengan celdas <td>
            Elements filas = doc.select("table#grid tr:has(td)");
            logger.debug("🔍 Filas encontradas: " + filas.size());

            // Si no encuentra con ese selector, intenta alternativas
            if (filas.size() == 0) {
                logger.warn("⚠️ No se encontraron filas con 'table#grid tr:has(td)', intentando selectores alternativos...");
                filas = doc.select("table tr:has(td)");
                logger.debug("   Filas con 'table tr:has(td)': " + filas.size());

                if (filas.size() == 0) {
                    filas = doc.select("tr");
                    logger.debug("   Total de <tr> en el HTML: " + filas.size());
                }
            }

            for (int i = 0; i < filas.size(); i++) {
                Element fila = filas.get(i);
                Elements celdas = fila.select("td.vertical-align-middle");

                if (celdas.size() == 0) {
                    celdas = fila.select("td");
                }

                logger.debug("📍 Fila " + i + ": " + celdas.size() + " celdas encontradas");

                if (celdas.size() >= 6) {
                    Element editLink = celdas.get(0).select("a[cmd-list-edit], a[title=Editar]").first();
                    String editHref = editLink != null ? editLink.attr("href") : "";
                    String id = extractEditEmployeeId(editHref);
                    String idRegistroEntrada = extractHrefParameter(editHref, "id_registro_entrada");
                    String idRegistroSalida = extractHrefParameter(editHref, "id_registro_salida");

                    // Bloque IN
                    Element divIn = celdas.get(1).select("div:has(.site-grid-IN)").first();
                    String fullTextIn = "";
                    if (divIn != null) {
                        fullTextIn = divIn.select(".registro-manual").text();

                        logger.debug("   ➡️ IN: " + fullTextIn);

                    }

                    // Bloque OUT
                    Element divOut = celdas.get(1).select("div:has(.site-grid-OUT)").first();
                    String fullTextOut = "";
                    if (divOut != null) {
                        fullTextOut = divOut.select(".registro-manual").text();

                        logger.debug("   ⬅️ OUT: " + fullTextOut);

                    }

                    if (!fullTextIn.isEmpty() || !fullTextOut.isEmpty()) {
                        String[] partsIn = fullTextIn.split(" ");
                        String[] partsOut = fullTextOut.split(" ");

                        org.tenea.dto.TimeEntryRecord record = new org.tenea.dto.TimeEntryRecord();
                        record.setId(id);
                        record.setIdRegistroEntrada(idRegistroEntrada);
                        record.setIdRegistroSalida(idRegistroSalida);
                        record.setFecha(partsIn.length > 0 && !partsIn[0].isEmpty() ? partsIn[0] : (partsOut.length > 0 ? partsOut[0] : ""));
                        record.setHoraIn(partsIn.length > 1 ? partsIn[1] : "");
                        record.setHoraOut(partsOut.length > 1 ? partsOut[1] : "");
                        record.setZona(celdas.get(2).select(".no-wrap").first() != null ?
                                celdas.get(2).select(".no-wrap").first().text() : "");
                        listado.add(record);
                    }
                }
            }

            logger.info("✅ Total registros parseados: " + listado.size());
            return listado;
        }
    }

    private String extractEditEmployeeId(String href) {
        if (href == null || href.isEmpty()) {
            return "";
        }
        Matcher matcher = Pattern.compile("/EditEmployee/([^?]+)").matcher(href);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractHrefParameter(String href, String parameterName) throws Exception {
        if (href == null || href.isEmpty()) {
            return "";
        }
        String normalizedHref = href.replace("&amp;", "&");
        Matcher matcher = Pattern.compile("[?&]" + Pattern.quote(parameterName) + "=([^&]+)").matcher(normalizedHref);
        if (!matcher.find()) {
            return "";
        }
        return URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
    }
}
