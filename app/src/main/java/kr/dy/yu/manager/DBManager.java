package kr.dy.yu.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by simjo on 2016-05-25.
 */
public class DBManager {
    Context myContext = null;

    static final String _dbName = "data.db";
    static int _dbVersion = 1;

    private static DBManager uniqueInstance = null;
    protected SQLiteDatabase database = null;
    private Cursor getData;

    public static DBManager getInstance(Context context){
        if(uniqueInstance == null){
            uniqueInstance = new DBManager(context);
        }
        return uniqueInstance;
    }

    public static void ResetInstance(Context context){
        uniqueInstance = new DBManager(context);
    }

    private DBManager(Context context) {
        myContext = context;

        // 파일이 존재하지 않으면!
        if(!context.getDatabasePath("data.db").exists()){
           _dbVersion = -1;
        }
        else{
            // 데이터 베이스 열기
            database = context.openOrCreateDatabase(_dbName, Context.MODE_PRIVATE, null);
            // db의 버전정보 획득!
            getData = database.query("version", new String[]{"sum"},
                                        null, null, null, null, null, null);
            getData.moveToFirst();
            _dbVersion = getData.getInt(0);
        }
    }
}
