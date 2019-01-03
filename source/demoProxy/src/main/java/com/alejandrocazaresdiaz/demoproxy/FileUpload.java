package com.alejandrocazaresdiaz.demoproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Alex Cazares
 */
public class FileUpload {

    public void uploadDemo(ServletContext context, HttpServletRequest request, Requester http) throws Exception{
        Date timeIn = new Date();
        Collection<Part> parts = request.getParts();
        if (parts != null) {
            for (Part p : parts) {
                byte[] buffer = new byte[p.getInputStream().available()];
                p.getInputStream().read(buffer);
                String uploadPath = context.getRealPath("") + File.separator + p.getSubmittedFileName();
                File targetFile = new File(uploadPath);
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);
                outStream.close();

//                FileUtils.copyInputStreamToFile(p.getInputStream(), targetFile);
                boolean repeat = true;
                for (int i = 0; i < 10 && repeat && 180000 > new Date().getTime() - timeIn.getTime(); i++) {
                    String rsp = "";
                    try {
                        rsp = postMethod(targetFile.getAbsolutePath());
                        JSONObject jsonRsp = new JSONObject(rsp);
                        if (jsonRsp.has("productBase")) {
                            repeat = false;
                        } else if (jsonRsp.has("errorCode") && jsonRsp.getString("errorCode").equals("BAD_REQUEST")) {
                            //replace sbe for any other
                            repeat = false;
                        } else if (jsonRsp.has("BAD_REQUEST")) {
                            //replace sbe for any other
                            repeat = false;
                        } else if (jsonRsp.has("INVALID_INPUT")) {
//                    repeat = true; // no actions, repeat process
                        }//                
                        System.out.println(i + ":" + rsp);
                    } catch (JSONException jsonEx) {
                        System.out.println("Warnning: CtlrSBE " + jsonEx.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                targetFile.delete();
            }
        }
    }
    
    
    
    public void uploadDemo(ServletContext context, MultipartFile multiPartF, Requester http){
        if (multiPartF != null && !multiPartF.isEmpty()) {
            Date in = new Date();
            boolean repeat = true;
            String uploadPath = context.getRealPath("") + File.separator;
            File convFile = new File(uploadPath + multiPartF.getOriginalFilename());

            for (int i = 0; i < 10 && repeat; i++) {
                String rsp = "";
                try {
                    FileCopyUtils.copy(multiPartF.getBytes(), convFile);
                    rsp = postMethod(convFile.getAbsolutePath());
                    convFile.delete();
                    JSONObject jsonRsp = new JSONObject(rsp);
                    if (jsonRsp.has("productBase")) {
                        repeat = false;
                    } else if (jsonRsp.has("BAD_REQUEST")) {
                        //replace sbe for any other
                    } else if (jsonRsp.has("INVALID_INPUT")) {
//                    repeat = true; // no actions, repeat process
                    }//                
                    System.out.println(i + ":" + rsp);
                } catch (JSONException jsonEx) {
                    System.out.println("Warnning: CtlrSBE " + jsonEx.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println(new Date().getTime() - in.getTime() + " ms (response)");
        }
    }
    
    //    public static void main(String[] args) {
    public String postMethod(String pathFile) {
        try {
//            HttpPost post = new HttpPost("http://dghouesbe205.w3-969.ibm.com:8000/session/restore");            
            HttpPost post = new HttpPost("http://dgdalesbe101.w3-969.ibm.com:8000/session/restore");
            CloseableHttpClient client = HttpClients.createDefault();

//            File file = new File("C:\\Users\\IBM_ADMIN\\Downloads\\115619700551330.ser");
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


}
