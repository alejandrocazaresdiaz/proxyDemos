package com.alejandrocazaresdiaz.demoproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 *
 * @author AlexCazares
 */
@Controller
@EnableAutoConfiguration
public class Ctrl {

    //
    //http://www.baeldung.com/httpclient-post-http-request
    //
    @Autowired
    Requester http;

    @Autowired
    ServletContext context;

//    @RequestMapping(value = "/session", produces = MediaType.ALL_VALUE, method = RequestMethod.POST)
//    @ResponseBody
//    ResponseEntity<String> newSession(@RequestBody String requestBody, HttpServletRequest request) {//traceTypes
//        return new RequestNewSession(requestBody, request, http, balancer).response();
//    }

//    @RequestMapping(value = "/session/{sessionID}/serializedSession", method = RequestMethod.GET)
//    @ResponseBody
//    ResponseEntity serializedSession(@RequestBody(required = false) String requestBody,
//            @PathVariable("sessionID") String sessionID, HttpServletRequest request) {
//        try {
//            return balancer.serializedSession(request, http, sessionID);
//        } catch (Exception e1) {//catch (HttpClientErrorException httpCeE) {
//            try {
//                System.out.println("Try http.processSession()...");
//                http.productBaseProvider = balancer.prepareSession(request, sessionID);
//                return http.processSession(requestBody);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        System.err.println("ERROR: CtrlSBE.serializedSession()");
//        return null;
//    }

//    @RequestMapping(value = "/session/{sessionID}/**", produces = MediaType.ALL_VALUE)
//    @ResponseBody
//    ResponseEntity<String> sessionID(@RequestBody(required = false) String requestBody,
//            @PathVariable("sessionID") String sessionID, HttpServletRequest request
//    ) {
//        try {
//            http.productBaseProvider = balancer.prepareSession(request, sessionID);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return http.processSession(requestBody);
//
//    }

//    @RequestMapping(value = "/session/restore", produces = MediaType.ALL_VALUE, method = RequestMethod.POST)
//    @ResponseBody
//    ResponseEntity<String> sessionRestorePatch(@RequestBody(required = false) String requestBody, HttpServletRequest request,
//            @RequestParam(name = "file", required = false) MultipartFile file
//    ) {
//        return balancer.sessionRestorePatch(request, http);
//    }

    public void loadProperties() {
        Properties prop = new Properties();
        InputStream input = null;

//        System.out.println("Find properties::"+System.getProperty("user.dir"));
        File currentFolder = new File(System.getProperty("user.dir"));
        for (final File fileEntry : currentFolder.listFiles()) {
            String sbeDef = fileEntry.getName();
            //System.out.println("file: " + sbeDef);
            if (fileEntry.isFile() && sbeDef.contains(".properties")) {

                try {
                    input = new FileInputStream(sbeDef);//("config.properties");
                    // load a properties file
                    prop.load(input);
                    prop.getProperty("type");
                } catch (Exception ex) {
                    Logger.getLogger(Ctrl.class.getName()).log(Level.SEVERE, "", ex);
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            Logger.getLogger(Ctrl.class.getName()).log(Level.SEVERE, "", e);
                        }
                    }
                }
            }
        }
    }

//    public void test(String[] args) {
//        if (false && file != null) {
//            System.out.println("Procesando archivo: " + file.getName());
//            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//            builder.addBinaryBody(file.getName(), file.getBytes(), ContentType.MULTIPART_FORM_DATA, file.getOriginalFilename());
//            builder.addTextBody(file.getName(), new String(file.getBytes(), StandardCharsets.UTF_8), ContentType.MULTIPART_FORM_DATA);
//            System.out.println("ContentType.DEFAULT_BINARY:" + ContentType.MULTIPART_FORM_DATA);
//            System.out.println(new String(file.getBytes(), StandardCharsets.UTF_8));
////            builder.addBinaryBody(file.getName(), file.getBytes(), ContentType.DEFAULT_BINARY, file.getOriginalFilename());
////            builder.addTextBody(file.getName(), new String(file.getBytes(), StandardCharsets.UTF_8), ContentType.DEFAULT_BINARY);
////            System.out.println("ContentType.DEFAULT_BINARY:"+ContentType.DEFAULT_BINARY);
////            builder.setBoundary(BOUNDARY);
//            http.setEntity(builder.build());
//
//        } else {
////    System.out.println("No hay archivo");
//            File file = new File("C:path\\61851728706823.ser");
//            FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
//            System.out.println("file: " + file.getName());
//            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//            builder.addPart("file", fileBody);
////builder.
//            http.setEntity(builder.build());
//        }
////            List<Part> parts = new ArrayList(request.getParts());
////            if (parts.size() > 0) {
//////                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//////                builder.addBinaryBody("file", new File("test.txt"),ContentType.APPLICATION_OCTET_STREAM
//////                , "file.ext");
//////                HttpEntity multipart = builder.build();
//////                http.setEntity(multipart);
////
//////                new StringEntity(parts);
////                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
////            }
//
//    }
    @RequestMapping(value = "_test/session", produces = MediaType.ALL_VALUE)
    @ResponseBody
    ResponseEntity<String> newSessionUnderConstruction(@RequestBody(required = false) String requestBody, HttpServletRequest request) {
//        for (String key : request.getParameterMap().keySet()) {
//            System.out.println(key + ": " + request.getParameter(key));
//        }
        try {
            if (request.getParts() != null) {
                List<Part> formData = new ArrayList(request.getParts());
                for (int i = 0; i < formData.size(); i++) {
                    Part part = formData.get(i);
                    System.out.println("formdata-size: " + formData.size());
//            Part part = request.getPart("file1");
                    String parameterName = part.getName();
                    System.out.println("parameterName: " + parameterName);
                    System.out.println("Size: " + part.getSize());
                    System.out.println("ContentType: " + part.getContentType());
                    System.out.println("SubmittedFileName: " + part.getSubmittedFileName());
//            logger.info("STORC IMAGES - " + parameterName);
                }
            }
        } catch (ServletException se) {
            System.err.println("Managed (ServletException)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Multipart start: ");
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

            Set set = multipartRequest.getFileMap().entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                String fileName = (String) me.getKey();
                MultipartFile multipartFile = (MultipartFile) me.getValue();
                System.out.println("Original fileName - " + multipartFile.getOriginalFilename());
                System.out.println("fileName - " + fileName);
//                writeToDisk(fileName, multipartFile);
            }
            System.out.println("Multipart end");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return http.unexpected("{\"testing\":\"true\"}", HttpStatus.ACCEPTED);
//        return new RequestNewSession(requestBody, request, http, balancer).response();
    }

    @RequestMapping(value = "test1/session/restore", produces = MediaType.ALL_VALUE, method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<String> fileTestProcessing1(HttpServletRequest request) throws Exception {
        new FileUpload().uploadDemo(context, request, http);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>("ready1", responseHeaders, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "test/session/restore", produces = MediaType.ALL_VALUE, method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<String> fileTestProcessing(@RequestPart("file") MultipartFile multiPartF, HttpServletRequest request) throws Exception {
        new FileUpload().uploadDemo(context, multiPartF, http);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>("ready", responseHeaders, HttpStatus.ACCEPTED);

    }
}
