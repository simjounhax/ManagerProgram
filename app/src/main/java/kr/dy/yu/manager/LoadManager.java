package kr.dy.yu.manager;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by simjo on 2016-05-05.
 */

public class LoadManager extends MainActivity{

    // 접속대상 서버의 주소를 가진 객체
    private URL url;
    //통신을 담당하는 객체
    private HttpURLConnection conn;
    // 할 기능
    private String action;
    // 전달할 버전
    private int _version = 0;

    // 필요한 객체 초기화
    public LoadManager(){
        try{
            // 보낼 URL 주소를 설정
            url = new URL("http://36.39.144.65:8084/app/index.jsp");
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void SetAction(String toDo){
        this.action = toDo;
    }

    public void SetVersion(int v){
        this._version = v;
    }

    public String Login(String... params){
        String id = params[0].toString();
        String pw = params[1].toString();
        String param = "id=" + id + "&pw=" + pw + "&action=" + "login" + "&version=" + _version;

        return request(param);
    }

    public String Register(String... params){
        String id = params[0].toString();
        String pw = params[1].toString();
        String shopName = params[2].toString();
        String representative = params[3].toString();
        String address = params[4].toString();
        String cellPhone = params[5].toString();
        String phone = params[6].toString();
        String licenseeNumber = params[7].toString();
        String freightBranch = params[8].toString();
        String faxNumber = params[9].toString();

        String param = "id=" + id + "&pw=" + pw + "&shopname=" + shopName + "&representative=" + representative
                    + "&address=" + address + "&cellphone=" + cellPhone + "&phone=" + phone + "&licensee="
                + licenseeNumber + "&freight=" + freightBranch + "&fax=" + faxNumber + "&action=" + "register";

        return request(param);
    }

        public String request(String parameter){

        // 웹서버로 넘겨줄 파라미터들
        String params = parameter;
        // 결과 데이터를 담을 문자열을 하나 만든다.
        String parsedData = "";
        // 결과 통지문
        String result = "";

        BufferedReader buff = null;

        try{
            conn = (HttpURLConnection)url.openConnection();

            // 보내는 데이터를 설정
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 받는 데이터를 JSON 타입으로 설정
            conn.setRequestProperty("Accept", "application/json");

            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStream os = conn.getOutputStream();
            os.write(params.getBytes("EUC-KR"));
            os.flush();

            //웹 서버에 요청하는 시점
            conn.connect();
            // 웹 서버로부터 전송받을 데이터에 대한 스트림 얻기
            InputStream is = conn.getInputStream();


            // 1byte 기반의 바이트스트림이므로 한글이 깨진다.
            // 따라서 버퍼처리된 문자기반의 스트림으로 업그레이드 해야 한다.
            buff = new BufferedReader(new InputStreamReader(is));

            // 스트림을 얻어 왔으므로, 문자열로 반환
            StringBuffer str = new StringBuffer();
            String d = null;

            while((d=buff.readLine())!= null){
                str.append(d);
            }

            // HTML 구조를 파싱하는 부분
            Document doc = Jsoup.parse(str.toString());

            // 갖고 온 전체 HTML DOM에서 필요한 부분을 추출한다.
            Elements resultData = doc.select("body");

            for(Element r : resultData)
            {
                parsedData = r.text();
            }

            JSONObject json = new JSONObject(parsedData);
            JSONArray jArray = json.getJSONArray("Result");

            json = jArray.getJSONObject(0);

            if(action.equals("login")) {
                // 아이디, 비번 맞는지 확인

                result = json.getString("data");
                if(!result.equals("wrong")){
                    // 승인된 회원 여부 점검
                    result = json.getString("authorize");
                    if(result.equals("unauthorized")){
                        // 미승인 된 경우
                        return "unauthorized";
                    }
                    // 버젼 확인
                    result = json.getString("version");
                    if(result.equals("accordance")){
                        // 버젼 일치
                        return "next";
                    }
                    else if (result.equals("discordance")){
                        // 버전 불일치
                        return "discordance";
                    }
                }
                else{
                    return result;
                }
            }
            // 회원 등록하는 경우
            else if (action.equals("register")){
                // 중복부터 확인
                result = json.getString("update");
                if(result.equals("duplicate")){
                    // 중복
                    return "duplicate";
                }
                else if (result.equals("fail")){
                    return "fail";
                }
                else{
                    // 버전 확인
                    result = json.getString("update");
                    if(result.equals("accordance")){
                        // 버전 일치
                        return "accordance";
                    }
                    else{
                        // 버전 불일치
                        return "discordance";
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            if(buff!=null){
                try{
                    buff.close();
                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(result.equals("")) {
            return "timeout";
        }
        else{
            return result;
        }
    }

    public static void WriteFile(InputStream is, OutputStream os) throws IOException {
        int length = 0;
        final int BUFFER_SIZE = 4096;
        byte[] outByte = new byte[BUFFER_SIZE];
        while((length = is.read(outByte, 0, BUFFER_SIZE)) != -1)
            os.write(outByte, 0, length);
        os.flush();
        is.close();
        os.close();
    }
}
