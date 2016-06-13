package kr.dy.yu.manager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by simjo on 2016-05-24.
 */

public class MyAsyncTask extends AsyncTask<String, String, String> {

    private String action = "";
    MainActivity context;
    ProgressDialog pDialog = null;

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
            if(result.equals("discordance") == true)
            {
                publishProgress("DB 다운로드");
                this.FileDownload();
                return "next";
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
            Toast.makeText(context, "알 수 없는 이유로 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

        else if (s.equals("timeout")){
            Toast.makeText(context, "시간이 초과되었습니다. 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
        }

        else if (s.equals("unauthorized")){
            Toast.makeText(context, "미승인된 회원입니다. 나중에 다시 시도해 주십시오.", Toast.LENGTH_SHORT).show();
        }

        else{
            Toast.makeText(context, "알 수 없는 에러가 발생하였습니다!", Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(s);
    }

    private void FileDownload(){
        // 버전 불일치, 다운로드 필요.
        try {
            InputStream inputStream = new URL("http://36.39.144.65:8084/app/db/data.db").openStream();

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
}
