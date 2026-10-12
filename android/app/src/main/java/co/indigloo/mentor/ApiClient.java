package co.indigloo.mentor;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public class ApiClient {
    public String base="", token="", session="";
    public JSONObject request(String path,String method,JSONObject body) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(base+path).openConnection();
        c.setInstanceFollowRedirects(false);c.setConnectTimeout(10000);c.setReadTimeout(35000);c.setRequestMethod(method);
        c.setRequestProperty("Content-Type","application/json");if(!token.isEmpty())c.setRequestProperty("Authorization","Bearer "+token);
        try {
            if(body!=null){c.setDoOutput(true);try(OutputStream s=c.getOutputStream()){s.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
            int code=c.getResponseCode();if(code==401){token="";session="";}
            if(code<200||code>=300)throw new IOException("Server returned "+code+". Check account, connection, and server settings.");
            if(code==204)return new JSONObject();
            try(InputStream s=c.getInputStream();ByteArrayOutputStream b=new ByteArrayOutputStream()){byte[] buf=new byte[4096];int n;while((n=s.read(buf))!=-1){b.write(buf,0,n);if(b.size()>1048576)throw new IOException("Response too large");}return new JSONObject(b.toString("UTF-8"));}
        } finally {c.disconnect();}
    }
    public JSONObject chat(String q,String band,String lang,String mode,boolean model) throws Exception {
        if(session.isEmpty())session=request("/v1/sessions","POST",new JSONObject().put("band",band).put("lang",lang)).getString("id");
        return request("/v1/chat","POST",new JSONObject().put("session_id",session).put("question",q).put("mode",mode).put("use_model",model));
    }
}
