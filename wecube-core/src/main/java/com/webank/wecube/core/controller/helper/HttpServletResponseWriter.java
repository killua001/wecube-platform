package com.webank.wecube.core.controller.helper;

import com.webank.wecube.core.commons.WecubeCoreException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class HttpServletResponseWriter {

    private HttpServletResponse response;

    public HttpServletResponseWriter(HttpServletResponse response) {
        this.response = response;
    }

    public void writeHttpResponse(ResponseEntity<byte[]> responseEntity) {
        writeHeaders(responseEntity.getHeaders());
        writeBody(responseEntity.getBody());
    }

    private void writeHeaders(HttpHeaders headers) {
        if (headers == null || headers.isEmpty()) return;
        for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
            String headerName = headerEntry.getKey();
            List<String> headerValues = headerEntry.getValue();
            if (isNotEmpty(headerValues)) {
                headerValues.forEach(headerValue -> response.addHeader(headerName, headerValue));
            }
        }
    }

    private void writeBody(byte[] body) {
        if (body == null) return;
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(body);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            throw new WecubeCoreException(String.format("Failed to write http servlet response data due to %s ", e.getMessage()));
        }
    }

}