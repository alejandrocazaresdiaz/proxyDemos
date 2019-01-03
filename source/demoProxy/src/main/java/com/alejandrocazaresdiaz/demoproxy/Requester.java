package com.alejandrocazaresdiaz.demoproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * @author Alejandro Cazares
 */
@Service
public class Requester {
    
    //learning: http://www.baeldung.com/httpclient-post-http-request
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    private String requestBody;

    public ResponseEntity<String> responseEntity;
    private HttpHeaders responseHeaders;

    public Requester() {
        responseHeaders = new HttpHeaders();
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ResponseEntity<String> getResponseEntity() {
        return responseEntity;
    }

    public String buildURL() {
        String queryString = request.getQueryString();
        queryString = queryString == null ? "" : "?".concat(queryString);
        String uri = this.request.getRequestURI();
        return uri.concat(queryString);
    }

    public ResponseEntity<String> processSession(String requestBody) {
        this.requestBody = requestBody;

        Map<String, String> mapHeaders = new HashMap<String, String>();
        mapHeaders.put("content-type", request.getHeader("content-type"));
        mapHeaders.put("accept", request.getHeader("accept"));
//        Enumeration<String> e = request.getHeaderNames();
//        while (e.hasMoreElements()) {
//            String param = e.nextElement();
//            System.out.println(param);
//        }
        String url = buildURL();
        System.out.println("HttpService.processSession() proxy: " + url);
        doRequest(request.getMethod(), url, mapHeaders);

        response.setHeader("test", "test");
        return responseEntity;
    }

    private CloseableHttpResponse factoryHttp(CloseableHttpClient client, String method, String url, Map<String, String> headers) throws Exception {
        String[] listA = {"DELETE", "HEAD", "OPTIONS", "TRACE", "GET"};
        String[] listB = {"PATCH", "PUT", "POST"};
        method = method.toUpperCase();

        if (new ArrayList<String>(Arrays.asList(listA)).contains(method)) {
            HttpRequestBase http = null;
            switch (method) {
                case "DELETE":
                    http = new HttpDelete(url);
                    break;
                case "HEAD":
                    http = (new HttpHead(url));
                    break;
                case "OPTIONS":
                    http = (new HttpOptions(url));
                    break;
                case "TRACE":
                    http = (new HttpTrace(url));
                    break;
                case "GET":
                    http = (new HttpGet(url));
            }
            if (headers != null) {
                for (String header : headers.keySet()) {
                    http.setHeader(header, headers.get(header));
                }
                if (headers.get("authUser") != null && headers.get("authPass") != null) {
                    UsernamePasswordCredentials basicAuth = new UsernamePasswordCredentials(headers.get("authUser"), headers.get("authPass"));
                    http.addHeader(new BasicScheme().authenticate(basicAuth, http, null));
                }
            }
            return client.execute(http);
        }

        if (new ArrayList<String>(Arrays.asList(listB)).contains(method)) {
            HttpEntityEnclosingRequestBase http = null;
            switch (method) {
                case "POST":
                    http = new HttpPost(url);
                    break;
                case "PUT":
                    http = new HttpPut(url);
                    break;
                case "PATCH":
                    http = new HttpPatch(url);
                    break;
            }
            if (requestBody != null) {
                http.setEntity(new StringEntity(requestBody));
            }
            if (headers != null) {
                for (String header : headers.keySet()) {
                    http.setHeader(header, headers.get(header));
                }
                if (headers.get("authUser") != null && headers.get("authPass") != null) {
                    UsernamePasswordCredentials basicAuth = new UsernamePasswordCredentials(headers.get("authUser"), headers.get("authPass"));
                    http.addHeader(new BasicScheme().authenticate(basicAuth, http, null));
                }
            }
            MultipartEntityBuilder builder = addParts();
            if (builder != null) {
                http.setEntity(builder.build());
            }
//            Header[] h = http.getAllHeaders();
//            for (Header header : h) {
//                System.out.println(header.getName() + "::" + header.getValue());
//            }
            return client.execute(http);
        }

        return null;
    }
    
    

    public void doRequest(String method, String url, Map<String, String> headers) {
        try {
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true).build();

//            CloseableHttpClient client = HttpClients.custom()
//                    .setSSLContext(sslContext)
//                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
//                    .build();//ssh support
            
            int timeout = 60;//seconds
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();
            
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier())//ssh support
                    .setDefaultRequestConfig(config)//timeOut support
                    .build();

//            CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();//timeOut support
//            CloseableHttpClient client = HttpClients.createDefault();//default value
            CloseableHttpResponse response = factoryHttp(client, method, url, headers);
            if(true){//current A-1
                try {
                    responseHeaders.setContentType(MediaType.valueOf(response.getEntity().getContentType().getValue()));
                } catch (Exception ex1) {
                    responseHeaders.setContentType(MediaType.TEXT_PLAIN);
                }

                responseEntity = new ResponseEntity<String>(new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n")),
                        responseHeaders,
                        HttpStatus.resolve(response.getStatusLine().getStatusCode())
                );
            }
            if(false){//version A
                for (Header header : response.getAllHeaders()) {
                    responseHeaders.set(header.getName(), header.getValue());
                }
                responseHeaders.setContentLanguage(response.getLocale());

                InputStream is = response.getEntity().getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String message = IOUtils.toString(br);
                HttpStatus httpStatus = HttpStatus.resolve(response.getStatusLine().getStatusCode());
                responseEntity = new ResponseEntity<String>(message, responseHeaders, httpStatus);

//            responseEntity = new ResponseEntity<String>(br.lines().collect(Collectors.joining("\n")),
//                    responseHeaders,
//                    HttpStatus.resolve(response.getStatusLine().getStatusCode())
//            );
            }
            client.close();
        } catch (HttpHostConnectException httpEx) {
            String message = httpEx.getMessage();
            System.err.println("ERROR: HttpService:doRequest()::HttpHostConnectException " + message);
            responseHeaders.setContentType(MediaType.TEXT_PLAIN);
            responseEntity = new ResponseEntity<String>(
                    message, responseHeaders, HttpStatus.resolve(500));
        } catch (NoHttpResponseException noHttpExc) {
            String message = noHttpExc.getMessage();
            System.err.println("ERROR: HttpService:doRequest()::NoHttpResponseException " + message);
            responseHeaders.setContentType(MediaType.TEXT_PLAIN);
            responseEntity = new ResponseEntity<String>(
                    message, responseHeaders, HttpStatus.resolve(500));
        } catch (Exception e) {
            e.printStackTrace();
            responseHeaders.setContentType(MediaType.TEXT_PLAIN);
            responseEntity = new ResponseEntity<String>(
                    e.getMessage(), responseHeaders, HttpStatus.resolve(500));
        }
    }

    public ResponseEntity<String> unexpected(String msg, HttpStatus httpStatus) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<String>(msg, responseHeaders, httpStatus);//HttpStatus.BAD_REQUEST
    }

    //deprecated
    public String postMethod(String pathFile) {
        try {
            HttpPost post = new HttpPost("http://server:port/");
            CloseableHttpClient client = HttpClients.createDefault();

            File file = new File(pathFile);
            FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("file", fileBody);
            post.setEntity(builder.build());

            HttpResponse response = client.execute(post);

            String rsp = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));
//            System.out.println(rsp);
            client.close();
            return rsp;
        } catch (IOException ex) {
            Logger.getLogger(Requester.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Autowired
    ServletContext context;

    public MultipartEntityBuilder addParts() {
        MultipartEntityBuilder builder = null;
        try {
            Collection<Part> parts = request.getParts();
            if (parts != null && parts.size() > 0) {
                builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                for (Part p : parts) {
                    byte[] buffer = new byte[p.getInputStream().available()];
                    p.getInputStream().read(buffer);
                    String uploadPath = context.getRealPath("") + File.separator + p.getSubmittedFileName();
                    File file = new File(uploadPath);
                    OutputStream outStream = new FileOutputStream(file);
                    outStream.write(buffer);
                    outStream.close();

                    FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
                    builder.addPart(p.getName(), fileBody);
//                file.delete();
                }
            }
        } catch (IOException r) {
            System.err.println("WARNING: HttpService.addParts(IOException) " + r.getMessage());
        } catch (ServletException a) {
            System.err.println("WARNING: HttpService.addParts(ServletException) " + a.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder;
    }

    public String patchSessionRestore(MultipartEntityBuilder builder) throws Exception {
        String rsp = "";

        boolean repeat = true;
        try {
            String url = buildURL();
            HttpPost post = new HttpPost(url);
            post.setEntity(builder.build());
            CloseableHttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(post);

            rsp = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent())
            ).lines().collect(Collectors.joining("\n"));
            client.close();

            JSONObject jsonRsp = new JSONObject(rsp);
            if (jsonRsp.has("productBase")) {
                repeat = false;
            } else if (jsonRsp.has("errorCode") && jsonRsp.getString("errorCode").equals("BAD_REQUEST")) {
                repeat = false;
            }//sonRsp.has("INVALID_INPUT")) {//                                 
        } catch (JSONException jsonEx) {
            System.out.println(jsonEx.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rsp;
    }
}
