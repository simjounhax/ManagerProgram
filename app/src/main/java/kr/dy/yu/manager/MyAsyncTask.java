package kr.dy.yu.manager;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by simjo on 2016-05-24.
 */

public class MyAsyncTask extends AsyncTask<String, String, String> {

    private String action = "";
    MainActivity context;
    ProgressDialog pDialog = null;
    String[] checkList = new String[]{
            "l1","l2","l3","l4","l5","l6","l7","l8","l9","l10","l11","l12","l13","l14","l15","l16",
            "m1","m2","m3","m4","m5","m6","m7","m8","m9","m10","m11","m12","m13","m14","m15","m16",
            "m17","m18","m10","m20","m21","m22","m23","m24","m25","m26","m27","m28","m29","m30",
            "m31","m32","m33","m34", "m35","m36","m37","m38","m39","m40","m41","m42","m43"
    };
    public ArrayList<String> wrongList = new ArrayList<>();

    public MyAsyncTask(Context applicationContext) {
        context = (MainActivity) applicationContext;
    }

    public void SetAction(String actionToDo){
        this.action = actionToDo;
    }

    @Override
    protected void onPreExecute() {
        pDialog = ProgressDialog.show(
                /* 표시할 액티비티의 context*/ context,
                /* 다이얼로그 제목 문자열 */ "로그인",
                /* 다이얼로그 내용 문자열 */ "진행중...",
                /* 진행여부를 알 수 있는지 유뮤. true가 알 수 없음, false가 알 수 있음 */ true,
                /* 취소 가능 여부. 기본값 false -> 이전 키 및 바깥 영역 클릭해도 취소되지 않음 */ true
                /* 다이얼로그가 취소를 알리는 리스너 -> cancelListener*/
        );
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        pDialog.setTitle(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        if(action.equals("login")){
            context.loadMgr.SetAction("login");
            context.loadMgr.SetVersion(DBManager.getInstance(context)._dbVersion);
            result = context.loadMgr.Login(params);
            publishProgress("DB 확인 중");
            if(result.equals("discordance") == true)
            {
                publishProgress("DB 다운로드");
                if(DBManager.getInstance(context)._dbVersion == -1) {
                    this.FileDownload();
                    return "next";
                }
                else{
                    if((result = this.RenewDatabase()).equals("success")){
                        this.FileDownload();
                        return "next";
                    }
                    else if (result.equals("fail")){
                        return "renew_fail";
                    }
                }
            }
            return result;
        }
        else if (action.equals("register")){
            context.loadMgr.SetAction("register");
            result = context.loadMgr.Register(params);
            if(result.equals("discordance")){
                this.FileDownload();
                return "accordance";
            }
            return result;
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {

        // 프로그레스 다이얼로그 종료
        pDialog.dismiss();

        // 다음 화면으로 넘어간다.
        if(s.equals("next")){
            Intent next = new Intent(context, NavActivity.class);
            next.putExtra("id", context.id);
            context.startActivity(next);
        }

        // 잘못된 아이디, 비밀번호 메세지
        else if (s.equals("wrong")){
            Toast.makeText(context, "아이디 혹은 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
        }

        // 아이디 중복, 등록 실패 메세지.
        else if (s.equals("duplicate")){
            Toast.makeText(context, "아이디 중복입니다. 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

        // 버전 일치, 등록 성공 메세지.
        else if (s.equals("accordance")){
            Toast.makeText(context, "등록 되었습니다.", Toast.LENGTH_SHORT).show();
        }

        else if (s.equals("fail")){
            Toast.makeText(context, "알 수 없는 이유로 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

        else if (s.equals("timeout")){
            Toast.makeText(context, "시간이 초과되었습니다. 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
        }

        else if (s.equals("unauthorized")){
            Toast.makeText(context, "미승인된 회원입니다. 나중에 다시 시도해 주십시오.", Toast.LENGTH_SHORT).show();
        }

        else if (s.equals("renew_fail")){
            DBManager.getInstance(context)._dbVersion = -1;
            Toast.makeText(context, "DB 갱신에 문제가 생겼습니다. WIFI를 연결하고 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
        }

        else{
            Toast.makeText(context, "알 수 없는 에러가 발생하였습니다!", Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(s);
    }

    private void FileDownload(){
        // 버전 불일치, 다운로드 필요.
        try {
            InputStream inputStream = new URL("http://36.39.144.65:8084/db/data.db").openStream();

            // 파일이 존재하는지 확인한다.
            if(!context.getDatabasePath("data.db").exists()){
                // 존재하지 않으면
                context.openOrCreateDatabase(context.getDatabasePath("data.db").toString(), Context.MODE_PRIVATE, null);
            }
            File f = new File(context.getDatabasePath("data.db").toString());
            OutputStream out = new FileOutputStream(f);
            LoadManager.WriteFile(inputStream, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String RenewDatabase(){
        // 체크하기 위해 자체적으로 가지고 있는 Database를 가져온다.
        Cursor c = DBManager.getInstance(context).database.query(
                "version",
                checkList,
                null, null, null, null, null
        );
        c.moveToNext();
        // 확인을 진행한다
        for(int i = 0; i < this.checkList.length; i++){
            try {
                // 현재 비교해야 할 카테고리의 이름을 가져온다.
                String objectName = checkList[i];
                // 서버로 받은 데이터에서 해당 카테고리의 개수를 가져온다.
                int objectQuantity = MainActivity._resultObject.getInt(objectName);
                // 일치하지 않는 카테고리의 이름을 기록한다.
                if(c.getInt(i) != objectQuantity){
                    wrongList.add(objectName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 전체 데이터를 담는 객체
        JSONObject mainObject = new JSONObject();
        // 실제 데이터를 집합인 객체
        JSONObject dataObject = new JSONObject();

        // 마지막 과정에서 sum에서 지워주기 위한 값
        int count = 0;
        String[] classList = new String[wrongList.size()];
        try {
            /**
             * 개수가 일치하지 않는 분류의 보유하고 있는 제품의 아이디를 저장한다.
             */
            for(int i = 0; i < wrongList.size(); i++) {
                // 분야별 데이터를 담는 배열 -> 매번 새로 생성해줘야 한다! 기존의 데이터에 계속 쌓임
                JSONArray dataArray = new JSONArray();
                c = DBManager.getInstance(context).database.query("product",
                        new String[]{"id"},
                        "class=?",
                        new String[]{wrongList.get(i)},
                        null, null, null
                );
                while(c.moveToNext()){
                    dataArray.put(c.getInt(0));
                }
                mainObject.put(wrongList.get(i), dataArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return context.loadMgr.RequestDBRenew(mainObject);
    }
}
