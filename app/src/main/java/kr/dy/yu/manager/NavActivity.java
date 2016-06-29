package kr.dy.yu.manager;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class NavActivity extends AppCompatActivity implements View.OnClickListener, ActionBar.OnNavigationListener {
    LinearLayout listLayout;

    String[] parentItem = new String[]{
            "공구", "철물", "캠핑/사무", "생활/잡화", "랜턴", "칼", "신제품", "땡처리", "전체상품", "공지사항", "내\n주문보기", "내\n정보변경"
    };

    String[][] childitem = new String[][]{
            // 공구 중분류
            {"수작업공구", "측정공구", "에어공구", "절삭공구", "산업용품", "기타공구"},
            // 철물 중분류
            {"공구함", "공구가방", "철공용 공구", "목공용 공구", "농기구", "낫", "작두", "도끼", "망치",
                    "톱", "전자가위", "다목적가위", "주방가위", "양손가위", "관수용품", "기타 철물"},
            // 캠핑/사무 중분류
            {"커터칼", "칼날", "버너", "가스토치", "맥가이버", "기타"},
            // 생활/잡화 중분류
            {"주방용품", "욕실용품", "생활용품", "기타잡화"},
            // 랜턴 중분류
            {"작업등", "펜후레쉬", "헤드랜턴", "써치라이트", "줌라이트", "캡후래쉬", "기타 랜턴", "악세사리", "배터리"},
            // 칼 중분류
            {"칼", "야스리", "숫돌", "기타"}
    };

    // 수작업 공구 목록
    String[] handToolItem = new String[]{
            "뺀치\n롱로즈\n니퍼", "몽키스패너\n파이프렌치\n체인렌치", "바이스\n플라이어\n클램프", "소켓렌치",
            "스패너\n라쳇렌치\n기어렌치", "육각\n별\n볼렌치", "드라이버", "자동차공구", "전기,건설\n공구",
            "배관\n방폭\n캇타", "비트세트"
    };
    // 절삭 공구 목록
    String[] cuttingToolItem = new String[]{
            "기리", "절단", "팁쏘", "예초기날", "기타 절삭공구"
    };

    String[][] childClass = new String[][]{
            // 공구 중분류
            {"m0", "m1", "m2", "m0", "m3", "m4"},
            // 철물 중분류
            {"m5", "m6", "m7", "m8", "m9", "m10", "m11", "m12", "m13", "m14", "m15", "m16",
                    "m17", "m18", "m19", "m20"},
            // 캠핑/사무 중분류
            {"m21", "m22", "m23", "m24", "m25", "m26"},
            // 생활/잡화 중분류
            {"m27", "m28", "m29", "m30"},
            // 랜턴 중분류
            {"m31", "m32", "m33", "m34", "m35", "m36", "m37", "m38", "m39"},
            // 칼 중분류
            {"m40", "m41", "m42", "m43"}
    };

    int childItemLength = 0;
    long backPressedTime = 0;
    CountDownTimer connectCheckTimer;
    Button[] parentButton = new Button[parentItem.length];
    Button[] childButton = null;
    Button[] handToolButton = new Button[handToolItem.length];
    Button[] cutterToolButton = new Button[cuttingToolItem.length];

    ArrayList<ArrayList<String>> mGroupList = new ArrayList<ArrayList<String>>();
    ArrayList<String> mChildList = null;
    ArrayList<Bitmap> mBitmapList = new ArrayList<Bitmap>();
    boolean MODE_ORDER = false;
    LinearLayout _Lpicture;
    LinearLayout _productName;
    LinearLayout _origin;
    LinearLayout _brand;
    LinearLayout _standard;
    LinearLayout _model;
    LinearLayout _cost;
    LinearLayout root;
    ScrollView rootscroll;
    Button _pickBtn;

    ImageView iv;

    TableLayout itemTable;
    TableRow firstRow;
    Toast toast;

    // 장바구니
    TableLayout shoppingCart;

    public static Context context;
    public Order order;
    public Inquiry inquiry;
    public GetDraft draft;

    int firstIndex = 0;
    int secondIndex = 0;

    int rowPosition = 0;
    int colPosition = 0;

    int parentPosition = 0;
    int childPosition = 0;


    boolean isFirstOpened = false;
    boolean isSecondOpened = false;
    boolean isThirdOpened = false;
    boolean isFourthOpened = false;
    boolean isFifthOpened = false;
    boolean isSixthOpened = false;

    // 수작업공구 위치
    int _handToolPostion = 0;

    // 절삭공구 위치
    int _cuttingToolPosition = 0;

    int grandsonPosition = 0;
    int grandDaughterPosition = 0;

    // 수작업 공구가 열린상태인지 아닌지
    boolean _isHandOpened = false;
    // 절삭공구가 열린상태인지 아닌지
    boolean _isCutterOpened = false;

    // 실제 제품의 id - 제품의 주문 수량
    HashMap<String, Integer> productQuantity = new HashMap<>();

    // editBox의 id - 실제 제품의 id
    HashMap<Integer, String> quantityMap = new HashMap<>();

    // picture의 id - 실제 제품의 id
    HashMap<Integer, String> _pictureMap = new HashMap<>();

    URL url;

    String productId = "";
    ArrayList productList = new ArrayList<>();

    String searchMode = "";

    private ArrayAdapter<String> mAdapter;

    // 갯수 정하는 EditText 삽입
    EditText editBox;

    LayoutInflater layoutInflator;

    GridLayout _pictureList;
    LinearLayout _pictureList2;

    // 장바구니 컬럼들
    TextView _deliveryMethod;
    TextView t_wholeNumber;
    TextView t_wholePrice;
    TextView productWholeNumber;
    TextView productWholePrice;
    Button _orderButton;

    Intent preData;
    String _id;

    TableRow memoRow;
    EditText _memo;

    int _wholePrice;
    int _wholeNumber;

    final String METHOD_PARCEL = "택배";
    final String METHOD_FREIGHT = "화물";
    final String METHOD_MANUALLY = "직접";

    // 주문 내역 업데이트용
    final int MODE_UPDATE = 1;
    // 주문 내역 승인 여부 확인용
    final int MODE_CONFIRM_CHECK = 2;
    // 주문서 가져오기
    final int MODE_ORDER_DRAFT = 3;
    // 회원정보 변경하기
    final int MODE_USER_INFO_CHANGE = 4;
    // 공지사항 갱신하기
    final int MODE_NOTICE_UPDATE = 5;

    ArrayList<String> _noticeContent = new ArrayList<>();

    LinearLayout wholeScrollLayout;
    LinearLayout rightArea;

    float xScale = 0.0f;
    float yScale = 0.0f;

    final float X_SCALE_UPPER_LIMIT = 1.0f;
    final float Y_SCALE_UPPER_LIMIT = 1.0f;
    final float X_SCALE_LOWER_LIMIT = 0.3f;
    final float Y_SCALE_LOWER_LIMIT = 0.3f;

    double distance = 0;

    ImageView image;
    ImageView description;
    ImageView detailImage;

    HorizontalScrollView hScrollView;

    String originalValue = "";

    JSONObject _userData;
    Connection connection;

    private GoogleApiClient client;

    public Bitmap ByteArrayToBitmap(byte[] picData) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(/*데이터 소스*/picData, /*시작점*/0,
                                      /*Decode할 길이*/picData.length);
        return bitmap;
    }

    public Bitmap ResizeBitmap(Bitmap bitmap, float wantHeight) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        if (height > wantHeight) {
            float percent = (float) (height / 100);
            float scale = (float) (wantHeight / percent);
            width *= (scale / 100);
            height *= (scale / 100);
        }

        return Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, true);
    }

    private class Connection extends NavActivity {
        // 접속대상 서버의 주소를 가진 객체
        private URL url;
        //통신을 담당하는 객체
        private HttpURLConnection conn;
        // 할 기능
        private String action;

        public Connection() {
            try {
                // 보낼 URL 주소를 설정
                url = new URL("http://36.39.144.65:8084/getUserInfo.jsp");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public JSONObject request(String parameter) throws IOException, JSONException {

            // 웹서버로 넘겨줄 파라미터들
            String params = "id=" + parameter;
            // 결과 데이터를 담을 문자열을 하나 만든다.
            String parsedData = "";
            // 결과 통지문
            String result = "";
            JSONObject json = null;

            BufferedReader buff = null;

            try {
                conn = (HttpURLConnection) url.openConnection();

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
                os.write(params.getBytes("utf-8"));
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

                while ((d = buff.readLine()) != null) {
                    str.append(d);
                }

                // HTML 구조를 파싱하는 부분
                Document doc = Jsoup.parse(str.toString());

                // 갖고 온 전체 HTML DOM에서 필요한 부분을 추출한다.
                Elements resultData = doc.select("body");

                for (Element r : resultData) {
                    parsedData = r.text();
                }

                json = new JSONObject(parsedData);
            }
            catch(Exception e){
                e.printStackTrace();
            }finally {
                if(buff != null) {
                    buff.close();
                }
                conn.disconnect();
                return json;
            }
        }
    }

    private class GetUserInfo extends AsyncTask<String, String, String> {

        NavActivity context;

        public GetUserInfo(NavActivity context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject result = context.connection.request(_id);
                _userData = result;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    private void ChangeUserData(final JSONObject userData) {
        final ScrollView userDataLayout = (ScrollView)((FrameLayout)View.inflate(this, R.layout.activity_main, null)).getChildAt(1);
        userDataLayout.setVisibility(View.VISIBLE);

        // 부모로부터 자식 제거
        ((FrameLayout)userDataLayout.getParent()).removeAllViews();
        // 돌아가기 버튼 제거
        ((LinearLayout)userDataLayout.getChildAt(0)).removeViewAt(2);

        // 제목 변경
        ((TextView)((LinearLayout)userDataLayout.getChildAt(0)).getChildAt(0)).setText("회원정보 변경");
        final GridLayout dataForm = ((GridLayout)((LinearLayout)userDataLayout.getChildAt(0)).getChildAt(1));

        // 다시쓰기, 등록하기 버튼 제거
        dataForm.removeViewAt(21);
        dataForm.removeViewAt(21);

        /* 기존에 회원 데이터를 비밀번호 제외하고 채워놓아야 함*/
        try {
            ((TextView)dataForm.getChildAt(1)).setText("사용할 아이디(변경불가)");
            // 아이디
            ((EditText)dataForm.getChildAt(2)).setText(userData.getString("id").toString());
            ((EditText)dataForm.getChildAt(2)).setFocusable(false);
            // 비밀번호
            ((EditText)dataForm.getChildAt(4)).setText("");
            // 상호명
            ((EditText)dataForm.getChildAt(6)).setText(userData.getString("shopname").toString());
            // 대표자명
            ((EditText)dataForm.getChildAt(8)).setText(userData.getString("representative").toString());
            // 주소지
            ((EditText)dataForm.getChildAt(10)).setText(userData.getString("address").toString());
            // 휴대폰 번호
            ((EditText)dataForm.getChildAt(12)).setText(userData.getString("cellphone").toString());
            // 유선전화
            ((EditText)dataForm.getChildAt(14)).setText(userData.getString("phone").toString());
            // 사업자등록번호
            ((EditText)dataForm.getChildAt(16)).setText(userData.getString("regitnum").toString());
            // 화물지점
            ((EditText)dataForm.getChildAt(18)).setText(userData.getString("freight").toString());
            // 팩스
            ((EditText)dataForm.getChildAt(20)).setText(userData.getString("fax").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("회원정보 변경하기");
        builder.setView(userDataLayout);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!((EditText)dataForm.getChildAt(4)).getText().toString().equals("")) {
                    LinearLayout l = new LinearLayout(context);
                    TextView txt = new TextView(context);
                    txt.setText("정말로 변경하시겠습니까?");
                    l.addView(txt);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("변경 확인");
                    builder.setView(l);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Update up = new Update(MODE_USER_INFO_CHANGE);
                            JSONObject data = new JSONObject();

                            try {
                                // 아이디
                                data.put("id", ((EditText)dataForm.getChildAt(2)).getText().toString());
                                // 비밀번호
                                data.put("pw", ((EditText)dataForm.getChildAt(4)).getText().toString());
                                // 상호명
                                data.put("shopname", ((EditText)dataForm.getChildAt(6)).getText().toString());
                                // 대표자명
                                data.put("representative", ((EditText)dataForm.getChildAt(8)).getText().toString());
                                // 주소지
                                data.put("address", ((EditText)dataForm.getChildAt(10)).getText().toString());
                                // 휴대폰 번호
                                data.put("cellphone", ((EditText)dataForm.getChildAt(12)).getText().toString());
                                // 유선전화
                                data.put("phone", ((EditText)dataForm.getChildAt(14)).getText().toString());
                                // 사업자등록번호
                                data.put("regitnum", ((EditText)dataForm.getChildAt(16)).getText().toString());
                                // 화물지점
                                data.put("freight", ((EditText)dataForm.getChildAt(18)).getText().toString());
                                // 팩스
                                data.put("fax", ((EditText)dataForm.getChildAt(20)).getText().toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            up.SetUserData(data);
                            up.execute("");
                        }
                    });
                    builder.show();
                    builder.setNegativeButton("취소", null);
                }
                else{
                    Toast.makeText(NavActivity.this, "바꿀 비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preData = getIntent();
        _id = preData.getStringExtra("id");
        GetUserInfo userInfo = new GetUserInfo(this);
        connection = new Connection();
        userInfo.execute(_id);

        order = new Order();
        inquiry = new Inquiry();
        draft = new GetDraft();

        DBManager.ResetInstance(this);

        setContentView(R.layout.activity_nav);

        _Lpicture = (LinearLayout) findViewById(R.id.picture) ;
        _productName = (LinearLayout) findViewById(R.id.productName);
        _origin = (LinearLayout) findViewById(R.id.origin);
        _brand = (LinearLayout) findViewById(R.id.brand);
        _standard = (LinearLayout) findViewById(R.id.standard);
        _model = (LinearLayout) findViewById(R.id.model);
        _cost = (LinearLayout) findViewById(R.id.cost);
        _pickBtn = (Button) findViewById(R.id.pickBtn);
        _pickBtn.setOnClickListener(new AddCart());
        context = this;

        itemTable = (TableLayout) findViewById(R.id.itemTable);
        shoppingCart = (TableLayout) findViewById(R.id.shoppingCart);

        firstRow = (TableRow) findViewById(R.id.firstRow);
        memoRow = (TableRow) findViewById(R.id.belowSecondRow);

        _memo = (EditText) memoRow.getChildAt(0);

        _deliveryMethod = (TextView) findViewById(R.id.deliveryNethod);
        t_wholeNumber = (TextView) findViewById(R.id.wholeNumber);
        t_wholePrice = (TextView) findViewById(R.id.wholePrice);
        _orderButton = (Button) findViewById(R.id.orderButton);
        _orderButton.setOnClickListener(new item_order());


        // 데이터를 받아오기 전까지는 숨겨놓는다.
        itemTable.setVisibility(View.INVISIBLE);

        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams.bottomMargin = 15;

        for (int i = 0; i < childitem.length; i++) {
            for (int j = 0; j < childitem[i].length; j++) {
                childItemLength++;
            }
        }
        listLayout = (LinearLayout) findViewById(R.id.list);

        iv = new ImageView(this);
        iv.setImageResource(R.drawable.icon1);

        for (int i = 0; i < parentItem.length; i++) {
            parentButton[i] = new Button(this);
            parentButton[i].setId(IdGen._generateViewId());
            parentButton[i].setText(parentItem[i]);
            parentButton[i].setLayoutParams(lParams);
            parentButton[i].setBackgroundColor(Color.rgb(160, 160, 160));
            parentButton[i].setBackgroundResource(R.drawable.border);
            listLayout.addView(parentButton[i], i);

            // 공지사항, 전체상품, 신제품, 나의 주문보기
            if (i == parentItem.length - 1 || i == parentItem.length - 2 ||
                    i == parentItem.length-3 || i == parentItem.length-4 ||
                    i==parentItem.length-5 || i==parentItem.length-6) {
                parentButton[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 신제품
                        if(((Button)v).getText().equals(parentItem[parentItem.length-6])){
                            Search("new", "");
                        }

                        // 땡처리
                        if(((Button)v).getText().equals(parentItem[parentItem.length-5])){
                            Search("bigsale", "");
                        }

                        // 전체 상품
                        if(((Button)v).getText().equals(parentItem[parentItem.length-4])){
                            Search("all", "");
                        }

                        // 공지 사항
                        if(((Button)v).getText().equals(parentItem[parentItem.length-3])){
                            Search("notice", "");
                        }

                        // 나의 주문보기
                        if(((Button)v).getText().equals(parentItem[parentItem.length-2])){
                            Search("history", "");
                        }

                        // 나의 정보변경
                        if(((Button)v).getText().equals(parentItem[parentItem.length-1])){
                            ChangeUserData(_userData);
                        }
                    }
                });
            } else {
                parentButton[i].setOnClickListener(this);
            }
        }


        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.width = 200;
        lp.height = 300;

        ImageView ad = new ImageView(this);
        ad.setImageResource(R.drawable.ad);
        ad.setId(IdGen._generateViewId());
        ad.setLayoutParams(lp);
        listLayout.addView(ad);

        wholeScrollLayout = (LinearLayout) findViewById(R.id.wholeScroll);
        rightArea = (LinearLayout) findViewById(R.id.rightArea);
        root = (LinearLayout) findViewById(R.id.root);

        itemTable.setScaleX(0.31f);
        itemTable.setScaleY(0.31f);
        itemTable.setPivotX(0.0f);
        itemTable.setPivotY(0.0f);
        hScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScroll);
        rootscroll = (ScrollView) findViewById(R.id.rootscroll);

        rootscroll.setOnTouchListener(new View.OnTouchListener() {

            int distanceX = 0;
            int distanceY = 0;
            int distanceXY = 0;
            float transscale = itemTable.getScaleX();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = (event.getAction() & MotionEvent.ACTION_MASK);
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        if(event.getPointerCount() == 2)
                        {
                            try {
                                itemTable.setPivotX(0);
                                itemTable.setPivotY(0);
                                distanceX = (int) event.getX(event.getPointerId(secondIndex)) - (int) event.getX(event.getPointerId(firstIndex));
                                distanceX = (int) event.getY(event.getPointerId(secondIndex)) - (int) event.getY(event.getPointerId(firstIndex));
                                distanceXY = (int) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                                Log.d("distanceXY : ", String.valueOf(distanceXY));
                                Log.d("distance----- : ", String.valueOf(event.getX(event.getPointerId(secondIndex))));
                                if (distance < distanceXY) {
                                    transscale += 0.02;
                                    if (transscale >= 0.75f)
                                    {
                                        transscale = 0.75f;
                                        itemTable.setScaleX(transscale);
                                        itemTable.setScaleY(transscale);
                                        distance = distanceXY;
                                        break;
                                    }
                                    itemTable.setScaleX(transscale);
                                    itemTable.setScaleY(transscale);
                                } else {
                                    transscale -= 0.02;
                                    if (transscale <= 0.31f)
                                    {
                                        transscale = 0.31f;
                                        itemTable.setScaleX(transscale);
                                        itemTable.setScaleY(transscale);
                                        distance = distanceXY;
                                        break;
                                    }

                                    itemTable.setScaleX(transscale);
                                    itemTable.setScaleY(transscale);
                                }
                                distance = distanceXY;

                            } catch (IllegalArgumentException ex) {
                                ex.printStackTrace();
                            }
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                }
                return false;
            }
        });

        hScrollView.setOnTouchListener(new View.OnTouchListener() {
            int x1 = 0;
            int y1 = 0;
            int x2 = 0;
            int y2 = 0;
            float transscale = itemTable.getScaleX();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = (event.getAction() & MotionEvent.ACTION_MASK);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        try {
                            x1 = (int) event.getX(event.getPointerId(firstIndex));
                            y1 = (int) event.getY(event.getPointerId(firstIndex));
                            firstIndex = event.getActionIndex();
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        try {
                            x2 = (int) event.getX(event.getPointerId(secondIndex));
                            y2 = (int) event.getY(event.getPointerId(secondIndex));
                            secondIndex = event.getActionIndex();
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        }
                        break;
                }
                if (itemTable.getScaleX() == 0.31f) {
                    hScrollView.scrollTo(0,0);
                    return true;
                } else
                    return false;
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public void onClick(View v) {
        int count = 0;

        boolean isParent = false;
        boolean isChild = false;
        boolean isGrandson = false;

        // 내가 클릭한 버튼이 부모 버튼인지 확인
        for (int i = 0; i < parentButton.length; i++) {
            if (v.getId() == parentButton[i].getId()) {
                parentPosition = i;
                isParent = true;
                break;
            }
        }

        // 부모 버튼이 아니라면 자식버튼인지 확인
        if (!isParent) {
            for (int i = 0; i < childButton.length; i++) {
                if (v.getId() == childButton[i].getId()) {
                    childPosition = parentPosition + i + 1;
                    isChild = true;
                    break;
                }
            }
        }

        // 손자 버튼인지 확인!
        if (!isChild && !isParent) {
            // 절삭공구인지 확인
            if (cutterToolButton[0] != null) {
                for (int i = 0; i < cuttingToolItem.length; i++) {
                    if (v.getId() == cutterToolButton[i].getId()) {
                        isGrandson = true;
                        break;
                    }
                }
            }

            // 수작업 공구인지 확인
            if (handToolButton[0] != null) {
                for (int i = 0; i < handToolItem.length; i++) {
                    if (v.getId() == handToolButton[i].getId()) {
                        isGrandson = true;
                        break;
                    }
                }
            }
        }

        // 부모 버튼을 클릭한 경우
        if (isParent) {
            int position = 0;
            // 첫 번째 버튼 클릭 시
            if (((Button) v).getText().toString().equals(parentItem[0])) {
                position = 0;
                if (isFirstOpened) {
                    DeleteButton(position + 1, childitem[parentPosition].length);
                    isFirstOpened = false;
                } else {
                    AddButton(position + 1, MakeButton(position));

                    isFirstOpened = true;
                }
            }
            // 두 번째 버튼 클릭 시
            else if (((Button) v).getText().toString().equals(parentItem[1])) {
                position = AddWeight(1);
                if (isSecondOpened) {
                    DeleteButton(position + 1, childitem[parentPosition].length);
                    isSecondOpened = false;
                } else {
                    AddButton(position + 1, MakeButton(position));
                    isSecondOpened = true;
                }
            }
            // 세 번째 버튼 클릭 시
            else if (((Button) v).getText().toString().equals(parentItem[2])) {
                position = AddWeight(2);
                if (isThirdOpened) {
                    DeleteButton(position + 1, childitem[parentPosition].length);
                    isThirdOpened = false;
                } else {
                    AddButton(position + 1, MakeButton(position));
                    isThirdOpened = true;
                }
            }
            // 네 번째 버튼 클릭 시
            else if (((Button) v).getText().toString().equals(parentItem[3])) {
                position = AddWeight(3);
                if (isFourthOpened) {
                    DeleteButton(position + 1, childitem[parentPosition].length);
                    isFourthOpened = false;
                } else {
                    AddButton(position + 1, MakeButton(position));
                    isFourthOpened = true;
                }
            }
            // 다섯번째 버튼 클릭 시
            else if (((Button) v).getText().toString().equals(parentItem[4])) {
                position = AddWeight(4);
                if (isFifthOpened) {
                    DeleteButton(position + 1, childitem[parentPosition].length);
                    isFifthOpened = false;
                } else {
                    AddButton(position + 1, MakeButton(position));
                    isFifthOpened = true;
                }
            }
            // 여섯번째 버튼 클릭 시
            else if (((Button) v).getText().toString().equals(parentItem[5])) {
                position = AddWeight(5);
                if (isSixthOpened) {
                    DeleteButton(position + 1, childitem[parentPosition].length);
                    isSixthOpened = false;
                } else {
                    AddButton(position + 1, MakeButton(position));
                    isSixthOpened = true;
                }
            }
        }

        // 손자 없는 자식 버튼들을 클릭한 경우
        else if (isChild) {
            for (int i = 0; i < childitem.length; i++) {
                for (int j = 0; j < childitem[i].length; j++) {
                    if (((Button) v).getText().equals(childitem[i][j])) {
                        Search(childClass[i][j], "class");
                    }
                }
            }
        }
    }

    int AddWeight(int position) {
        switch (position) {
            case 1:
                if (isFirstOpened) {
                    position += childitem[0].length;
                }
                return position;
            case 2:
                if (isFirstOpened) {
                    position += childitem[0].length;
                }
                if (isSecondOpened) {
                    position += childitem[1].length;
                }
                return position;
            case 3:
                if (isFirstOpened) {
                    position += childitem[0].length;
                }
                if (isSecondOpened) {
                    position += childitem[1].length;
                }
                if (isThirdOpened) {
                    position += childitem[2].length;
                }
                return position;
            case 4:
                if (isFirstOpened) {
                    position += childitem[0].length;
                }
                if (isSecondOpened) {
                    position += childitem[1].length;
                }
                if (isThirdOpened) {
                    position += childitem[2].length;
                }
                if (isFourthOpened) {
                    position += childitem[3].length;
                }
                return position;
            case 5:
                if (isFirstOpened) {
                    position += childitem[0].length;
                }
                if (isSecondOpened) {
                    position += childitem[1].length;
                }
                if (isThirdOpened) {
                    position += childitem[2].length;
                }
                if (isFourthOpened) {
                    position += childitem[3].length;
                }
                if (isFifthOpened) {
                    position += childitem[4].length;
                }
                return position;
            default:
                return position;
        }
    }

    Button[] MakeButton(int position) {
        childButton = new Button[childitem[parentPosition].length];
        for (int i = 0; i < childButton.length; i++) {
            childButton[i] = new Button(this);
            childButton[i].setId(IdGen._generateViewId());
            childButton[i].setText(childitem[parentPosition][i]);
            childButton[i].setBackgroundResource(R.drawable.mder);
            childButton[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            // 소분류가 있는 버튼들은 다른 이벤트 리스너를 등록!
            if (childButton[i].getText().equals("수작업공구") || childButton[i].getText().equals("절삭공구")) {
                childButton[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (((Button) v).getText().equals("수작업공구")) {
                            _handToolPostion = ((LinearLayout) v.getParent()).indexOfChild(v);
                            if (!_isHandOpened) {
                                for (int i = 0; i < handToolItem.length; i++) {
                                    handToolButton[i] = new Button(NavActivity.this);
                                    handToolButton[i].setId(IdGen._generateViewId());
                                    handToolButton[i].setText(handToolItem[i]);
                                    handToolButton[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    listLayout.addView(handToolButton[i], _handToolPostion + i + 1);
                                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lParams.setMargins(0,0,0,8);
                                    handToolButton[i].setLayoutParams(lParams);
                                    handToolButton[i].setBackgroundResource(R.drawable.sder);
                                    // 이벤트 리스너 등록
                                    handToolButton[i].setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            int pos = 0;
                                            for (int i = 0; i < handToolItem.length; i++) {
                                                pos = i + 1;
                                                if (((Button) v).getText().equals(handToolItem[i])) {
                                                    Search(String.format("l%d", pos), "class");
                                                }
                                            }
                                        }
                                    });
                                }
                                _isHandOpened = true;
                            } else {
                                for (int i = 0; i < handToolItem.length; i++) {
                                    listLayout.removeViewAt(_handToolPostion + 1);
                                }
                                _isHandOpened = false;
                            }
                        } else if (((Button) v).getText().equals("절삭공구")) {
                            _cuttingToolPosition = ((LinearLayout) v.getParent()).indexOfChild(v);
                            if (!_isCutterOpened) {
                                for (int i = 0; i < cuttingToolItem.length; i++) {
                                    cutterToolButton[i] = new Button(NavActivity.this);
                                    cutterToolButton[i].setId(IdGen._generateViewId());
                                    cutterToolButton[i].setText(cuttingToolItem[i]);
                                    cutterToolButton[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    listLayout.addView(cutterToolButton[i], _cuttingToolPosition + i + 1);
                                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lParams.setMargins(0,0,0,8);
                                    cutterToolButton[i].setLayoutParams(lParams);
                                    cutterToolButton[i].setBackgroundResource(R.drawable.sder);
                                    // 이벤트 리스너 등록
                                    cutterToolButton[i].setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            int pos = 11;
                                            for (int i = 0; i < cuttingToolItem.length; i++) {
                                                if (((Button) v).getText().equals(cuttingToolItem[i])) {
                                                    pos = i + 1 + handToolItem.length;
                                                    Search(String.format("l%d", pos), "class");
                                                    break;
                                                }
                                            }
                                        }
                                    });
                                }
                                _isCutterOpened = true;
                            } else {
                                for (int i = 0; i < cuttingToolItem.length; i++) {
                                    listLayout.removeViewAt(_cuttingToolPosition + 1);
                                }
                                _isCutterOpened = false;
                            }
                        }

                    }
                });
            } else {
                childButton[i].setOnClickListener(this);
            }
        }
        return childButton;
    }

    void DeleteButton(int position, int length) {
        for (int i = 0; i < length; i++) {
            listLayout.removeViewAt(position);
        }
    }

    void AddButton(int position, Button[] button) {
        for (int i = 0; i < button.length; i++) {
            listLayout.addView(button[i], position++);
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            lParams.setMargins(0,0,0,8);
            button[i].setLayoutParams(lParams);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        String item = mAdapter.getItem(itemPosition);
        return false;
    }

    private void Search(String keyword/*검색할 키워드*/, String searchType/*검색할 종류*/) {
        Cursor c;
        SQLiteDatabase db = DBManager.getInstance(this).database;
        _orderButton.setOnClickListener(new item_order());
        _pickBtn.setOnClickListener(new AddCart());
        _pickBtn.setText("장바구니 담기");
        _orderButton.setText("장바구니 보기");
        // 전체 상품, 신제품, 검색바 이용시
        if (searchType.equals("")) {
            switch (keyword) {
                case "all":
                    c = db.query("product",
                            new String[]{
                                    "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                            },
                            null, null, null, null, null);
                    Display(c);
                    break;
                case "new":
                    c = db.query("product",
                            new String[]{
                                    "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                            },
                            "isNew=1", null, null, null, null);
                    Display(c);
                    break;
                case "bigsale":
                    c = db.query("product",
                            new String[]{
                                    "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                            },
                            "bigsale=1", null, null, null, null);
                    Display(c);
                    break;
                // 공지사항 띄울 시
                case "notice":
                    new Update(MODE_NOTICE_UPDATE).execute();
//                    c = db.query("notice",
//                            new String[]{
//                                    "number", "header", "content", "date"
//                            },
//                            null, null, null, null,
//                            "number desc");
//                    DisplayNotice(c);
                    break;
                // 나의 주문내역
                case "history":
                    c = db.query("orders",
                            new String[]{"ordernum", "sum", "date"},
                            String.format("id='%s'", _id),
                            null, null, null, null
                    );
                    DisplayOrderHistory(c);
            }
        } else if (searchType.equals("search")) {
            if (searchMode.equals("제품명")) {
                c = db.query("product",
                        new String[]{
                                "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                        },
                        "name=" + String.format("'%s'", keyword),
                        null, null, null, null);
                Display(c);
            } else if (searchMode.equals("브랜드")) {
                c = db.query("product",
                        new String[]{
                                "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                        },
                        "brand=" + String.format("'%s'", keyword),
                        null, null, null, null);
                Display(c);
            } else if (searchMode.equals("규격")) {
                c = db.query("product",
                        new String[]{
                                "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                        },
                        "standard=" + String.format("\"%s\"", keyword),
                        null, null, null, null);
                Display(c);
            } else if (searchMode.equals("모델")) {
                c = db.query("product",
                        new String[]{
                                "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                        },
                        "model=" + String.format("'%s'", keyword),
                        null, null, null, null);
                Display(c);
            }
        }
        // 특정 카테고리 클릭 시
        else if (searchType.equals("class")) {
            c = db.query("product",
                    new String[]{
                            "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                    },
                    "class=" + String.format("'%s'", keyword), null, null, null, null);
            Display(c);
        }
    }

    public class LockableScrollView extends ScrollView {

        // true if we can scroll (not locked)
        // false if we cannot scroll (locked)
        private boolean mScrollable = false;

        public LockableScrollView(Context context) {
            super(context);
        }

        public LockableScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LockableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void setScrollingEnabled(boolean enabled) {
            mScrollable = enabled;
        }

        public boolean isScrollable() {
            return mScrollable;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // if we can scroll pass the event to the superclass
                    if (mScrollable) return super.onTouchEvent(ev);
                    // only continue to handle the touch event if scrolling enabled
                    return mScrollable; // mScrollable is always false at this point
                default:
                    return super.onTouchEvent(ev);
            }
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            // Don't do anything with intercepted touch events if
            // we are not scrollable
            if (!mScrollable) return false;
            else return super.onInterceptTouchEvent(ev);
        }

    }

    // 주문 내역을 우측에 나열하는 함수
    private void DisplayOrderHistory(Cursor c) {
        itemTable.removeAllViews();
        itemTable.setVisibility(View.VISIBLE);
        itemTable.setPadding(50, 100, 50, 0);

        TableRow row = new TableRow(this);
        row.setBackgroundResource(R.drawable.border);
        row.setPadding(30, 0, 30, 0);

        TableRow.LayoutParams trParams = new TableRow.LayoutParams();
        trParams.setMargins(30, 20, 30, 20);

        TextView orderNum = new TextView(this);
        TextView orderSum = new TextView(this);
        TextView orderDate = new TextView(this);

        orderNum.setText("주문번호");
        orderSum.setText("주문금액");
        orderDate.setText("주문일자");

        orderNum.setTextSize(20f);
        orderSum.setTextSize(20f);
        orderDate.setTextSize(20f);

        orderNum.setLayoutParams(trParams);
        orderSum.setLayoutParams(trParams);
        orderDate.setLayoutParams(trParams);

        row.addView(orderNum);
        row.addView(orderSum);
        row.addView(orderDate);

        itemTable.addView(row);

        // 주문 내역이 있을 경우
        if (c.getCount() != 0) {
            while (c.moveToNext()) {
                String number = c.getString(0);
                String sum = c.getString(1);
                String date = c.getString(2);

                orderNum = new TextView(this);
                orderSum = new TextView(this);
                orderDate = new TextView(this);

                orderNum.setText(number);
                orderSum.setText(sum);
                orderDate.setText(date);

                orderNum.setLayoutParams(trParams);
                orderSum.setLayoutParams(trParams);
                orderDate.setLayoutParams(trParams);

                orderNum.setGravity(Gravity.CENTER);
                orderSum.setGravity(Gravity.CENTER);
                orderDate.setGravity(Gravity.CENTER);

                TableRow itemRow = new TableRow(this);
                itemRow.setGravity(Gravity.CENTER);
                itemRow.setBackgroundResource(R.drawable.border);
                itemRow.setPadding(30, 0, 30, 0);
                itemRow.setLayoutParams(trParams);

                itemRow.addView(orderNum);
                itemRow.addView(orderSum);
                itemRow.addView(orderDate);

                itemTable.addView(itemRow);

                itemRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Update update = new Update(MODE_CONFIRM_CHECK);
                        String orderNumber = ((TextView) ((TableRow) v).getChildAt(0)).getText().toString();
                        update.execute(orderNumber);
                    }
                });
            }
        }
        // 주문내역이 없을 경우
        else {
            TableRow itemRow = new TableRow(this);

            TextView noOrders = new TextView(this);
            noOrders.setText("주문정보가 없습니다.");
            noOrders.setTextSize(30f);
            itemRow.addView(noOrders);
            itemTable.addView(itemRow);
        }
    }

    private void order_Display() {
        // 이미 생성돼 있는 View를 전부 삭제
        itemTable.removeAllViews();
        firstRow.removeAllViews();
        firstRow.addView(_Lpicture);
        firstRow.addView(_productName);
        firstRow.addView(_origin);
        firstRow.addView(_brand);
        firstRow.addView(_standard);
        firstRow.addView(_model);
        firstRow.addView(_cost);
        firstRow.addView(_pickBtn);
        itemTable.addView(firstRow);

        int count = 0;
        int dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());

        TableRow.LayoutParams trParams = new TableRow.LayoutParams();
        trParams.rightMargin = dp;

        if (mGroupList.size() == 0) {
            _orderButton.setText("장바구니 보기");
            Toast.makeText(this, "장바구니가 비어있습니다.", Toast.LENGTH_SHORT).show();
            itemTable.setVisibility(View.INVISIBLE);
        } else {
            _orderButton.setOnClickListener(new OrderProduct());
            while (count != mGroupList.size()) {
                TableRow.LayoutParams lParams  = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                TableRow newRow = new TableRow(this);
                newRow.setBackgroundResource(R.drawable.border);
                newRow.setGravity(Gravity.CENTER);
                LinearLayout area;

                // 사진, 이름, 원산지, 브랜드, 표준, 모델, 가격, 아이디
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                area.setLayoutParams(lParams);
                ImageView pic = new ImageView(NavActivity.this);
                area.addView(pic);
                Bitmap newPicture = mBitmapList.get(count);

                if (newPicture != null) {
                    // 높이를 50으로 재조정한다.
                    newPicture = ResizeBitmap(newPicture, 180);
                    lParams.width = newPicture.getWidth();
                    lParams.height = newPicture.getHeight();
                    Drawable picDrawable = new BitmapDrawable(getResources(), newPicture);
                    pic.setImageDrawable(picDrawable);
                    pic.setId(IdGen._generateViewId());
                    pic.setOnClickListener(new PictureEvent());
                    pic.setScaleX(0.9f);
                    pic.setScaleY(0.9f);
                    newRow.addView(area);
                } else {
                    TextView noPicture = new TextView(this);
                    noPicture.setText("사진 없음");
                    newRow.addView(noPicture);
                }
                TextView[] data = new TextView[6];

                // 이름
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[0] = new TextView(NavActivity.this);
                data[0].setId(IdGen._generateViewId());
                data[0].setText(mGroupList.get(count).get(0));
                data[0].setTextSize(20);
                data[0].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[0]);

                // 원산지
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[1] = new TextView(NavActivity.this);
                data[1].setId(IdGen._generateViewId());
                data[1].setText(mGroupList.get(count).get(1));
                data[1].setTextSize(20);
                data[1].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[1]);

                // 브랜드
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[2] = new TextView(NavActivity.this);
                data[2].setId(IdGen._generateViewId());
                data[2].setText(mGroupList.get(count).get(2));
                data[2].setTextSize(20);
                data[2].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[2]);
                // 규격
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[3] = new TextView(NavActivity.this);
                data[3].setId(IdGen._generateViewId());
                data[3].setText(mGroupList.get(count).get(3));
                data[3].setTextSize(20);
                data[3].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[3]);

                // 모델
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[4] = new TextView(NavActivity.this);
                data[4].setId(IdGen._generateViewId());
                data[4].setText(mGroupList.get(count).get(4));
                data[4].setTextSize(20);
                data[4].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[4]);

                // 공급단가
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[5] = new TextView(NavActivity.this);
                data[5].setId(IdGen._generateViewId());
                data[5].setText(mGroupList.get(count).get(5));
                data[5].setTextSize(20);
                data[5].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[5]);

                editBox = new EditText(this);
                editBox.setId(IdGen._generateViewId());
                editBox.setText(mGroupList.get(count).get(6));
                quantityMap.put(/*EditBox의 id*/editBox.getId(), productId);
                editBox.setGravity(Gravity.RIGHT);
                editBox.setTextSize(24);
                editBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20),new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart,
                                               int dend) {
                        Pattern ps = Pattern.compile("^[0-9]+$");
                        if (source.equals("") || ps.matcher(source).matches()) {
                            return source;
                        }
                        toast = Toast.makeText(getApplicationContext(), "숫자만 입력해주세요.", Toast.LENGTH_SHORT);
                        toast.show();
                        return "";
                    }
                }});

                area = new LinearLayout(this);
                area.setLayoutParams(lParams);
                area.setGravity(Gravity.CENTER);
                area.setBackgroundResource(R.drawable.cellborder);
                area.requestDisallowInterceptTouchEvent(true);
                area.addView(editBox);
                newRow.addView(area);

                // 장바구니에 담는다고 표시할 체크박스 삽입
                CheckBox cb = new CheckBox(NavActivity.this);
                cb.setId(IdGen._generateViewId());
                cb.setGravity(Gravity.CENTER_HORIZONTAL);
                cb.setScaleX(2.3f);
                cb.setScaleY(2.3f);
                cb.setChecked(true);

                area = new LinearLayout(this);
                area.setLayoutParams(lParams);
                area.setGravity(Gravity.CENTER);
                area.setBackgroundResource(R.drawable.cellborder);
                area.addView(cb);
                newRow.addView(area);

                /// 추가한 Row를 TableLayout의 child로 추가해야 한다.
                itemTable.addView(newRow);
                count++;
            }
            itemTable.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(927, -1);
            hScrollView.setLayoutParams(lParams);
        }
    }

    private void Display(Cursor c) {
        // 이미 생성돼 있는 View를 전부 삭제
        itemTable.removeAllViews();
        firstRow.removeAllViews();
        firstRow.addView(_Lpicture);
        firstRow.addView(_productName);
        firstRow.addView(_origin);
        firstRow.addView(_brand);
        firstRow.addView(_standard);
        firstRow.addView(_model);
        firstRow.addView(_cost);

        firstRow.addView(_pickBtn);

        itemTable.addView(firstRow);

        int dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());

        TableRow.LayoutParams trParams = new TableRow.LayoutParams();
        trParams.rightMargin = dp;

        if (c.getCount() == 0) {
            Toast.makeText(this, "검색결과가 없습니다!", Toast.LENGTH_SHORT).show();
            itemTable.setVisibility(View.INVISIBLE);
        } else {
            while (c.moveToNext()) {
                TableRow.LayoutParams lParams  = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                TableRow newRow = new TableRow(this);
                newRow.setBackgroundResource(R.drawable.border);
                newRow.setGravity(Gravity.CENTER);
                LinearLayout area;

                // 사진, 이름, 원산지, 브랜드, 표준, 모델, 가격, 아이디
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                area.setLayoutParams(lParams);
                ImageView pic = new ImageView(NavActivity.this);
                area.addView(pic);
                byte[] picByteData = c.getBlob(0);
                Bitmap newPicture = ByteArrayToBitmap(picByteData);
                if (newPicture != null) {
                    // 높이를 50으로 재조정한다.
                    newPicture = ResizeBitmap(newPicture, 180);
                    lParams.width = newPicture.getWidth() < 150 ? 150 : newPicture.getWidth();
                    lParams.height = newPicture.getHeight() < 150 ? 150 : newPicture.getWidth();
                    Drawable picDrawable = new BitmapDrawable(getResources(), newPicture);
                    pic.setImageDrawable(picDrawable);
                    pic.setId(IdGen._generateViewId());
                    pic.setOnClickListener(new PictureEvent());
                    pic.setScaleX(0.9f);
                    pic.setScaleY(0.9f);
                    newRow.addView(area);
                } else {
                    TextView noPicture = new TextView(this);
                    noPicture.setText("사진 없음");
                    newRow.addView(noPicture);
                }
                TextView[] data = new TextView[6];

                // 이름
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[0] = new TextView(NavActivity.this);
                data[0].setId(IdGen._generateViewId());
                data[0].setText(c.getString(1));
                data[0].setTextSize(20);
                data[0].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[0]);

                // 원산지
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[1] = new TextView(NavActivity.this);
                data[1].setId(IdGen._generateViewId());
                data[1].setText(c.getString(2));
                data[1].setTextSize(20);
                data[1].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[1]);

                // 브랜드
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[2] = new TextView(NavActivity.this);
                data[2].setId(IdGen._generateViewId());
                data[2].setText(c.getString(3));
                data[2].setTextSize(20);
                data[2].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[2]);
                // 규격
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[3] = new TextView(NavActivity.this);
                data[3].setId(IdGen._generateViewId());
                data[3].setText(c.getString(4));
                data[3].setTextSize(20);
                data[3].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[3]);

                // 모델
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[4] = new TextView(NavActivity.this);
                data[4].setId(IdGen._generateViewId());
                data[4].setText(c.getString(5));
                data[4].setTextSize(20);
                data[4].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[4]);

                // 공급단가
                area = new LinearLayout(this);
                area.setBackgroundResource(R.drawable.cellborder);
                data[5] = new TextView(NavActivity.this);
                data[5].setId(IdGen._generateViewId());
                data[5].setText(c.getString(6));
                data[5].setTextSize(20);
                data[5].setGravity(Gravity.CENTER);
                area.setGravity(Gravity.CENTER);
                area.setLayoutParams(lParams);
                newRow.addView(area);
                area.addView(data[5]);

                // 가져온 제품의 실제 DB에 등록된 아이디
                productId = c.getString(7);
                productList.add(productId);
                // ImageView와 제품 ID를 매치
                // ImageButton의 id - 실제 id
                _pictureMap.put(pic.getId(), productId);
                editBox = new EditText(this);
                editBox.setId(IdGen._generateViewId());
                editBox.setText("0");
                quantityMap.put(/*EditBox의 id*/editBox.getId(), productId);
                editBox.setGravity(Gravity.RIGHT);
                editBox.setTextSize(24);
                editBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20),new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart,
                                               int dend) {
                        Pattern ps = Pattern.compile("^[0-9]+$");
                        if (source.equals("") || ps.matcher(source).matches()) {
                            return source;
                        }
                        toast = Toast.makeText(getApplicationContext(), "숫자만 입력해주세요.", Toast.LENGTH_SHORT);
                        toast.show();
                        return "";
                    }
                }});

                area = new LinearLayout(this);
                area.setLayoutParams(lParams);
                area.setGravity(Gravity.CENTER);
                area.setBackgroundResource(R.drawable.cellborder);
                area.requestDisallowInterceptTouchEvent(true);
                area.addView(editBox);
                newRow.addView(area);

                // 장바구니에 담는다고 표시할 체크박스 삽입
                CheckBox cb = new CheckBox(NavActivity.this);
                cb.setId(IdGen._generateViewId());
                cb.setGravity(Gravity.CENTER_HORIZONTAL);
                cb.setScaleX(2.3f);
                cb.setScaleY(2.3f);

                area = new LinearLayout(this);
                area.setLayoutParams(lParams);
                area.setGravity(Gravity.CENTER);
                area.setBackgroundResource(R.drawable.cellborder);
                area.addView(cb);
                newRow.addView(area);

                /// 추가한 Row를 TableLayout의 child로 추가해야 한다.
                itemTable.addView(newRow);
            }
            itemTable.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(927, -1);
            hScrollView.setLayoutParams(lParams);
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Nav Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://kr.dy.yu.manager/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Nav Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://kr.dy.yu.manager/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    public void DisplayNotice(Cursor c) {
        // 우측 영역에 생성된 모든 Child 제거
        itemTable.removeAllViews();
        _noticeContent.clear();

        // 새로운 TextView 생성 -> 공지사항 번호, 제목, 날짜, 글쓴사람
        TextView noticeNumber = new TextView(this);
        TextView noticeHeader = new TextView(this);
        TextView noticeDate = new TextView(this);
        TextView noticeWriter = new TextView(this);

        // 새로운 TableRow를 추가
        TableRow row = new TableRow(this);
        row.setGravity(Gravity.CENTER);
        row.setBackgroundResource(R.drawable.border);
        row.setOnClickListener(new NoticeView());
        row.setPadding(20, 0, 20, 0);

        TableRow.LayoutParams trParams = new TableRow.LayoutParams();
        trParams.setMargins(30, 20, 30, 20);

        noticeNumber.setText("번호");
        noticeNumber.setTextSize(20);
        noticeNumber.setGravity(Gravity.CENTER);
        noticeNumber.setLayoutParams(trParams);
        row.addView(noticeNumber);

        noticeHeader.setText("제목");
        noticeHeader.setTextSize(20);
        noticeHeader.setGravity(Gravity.CENTER);
        noticeHeader.setLayoutParams(trParams);
        row.addView(noticeHeader);

        noticeDate.setText("날짜");
        noticeDate.setTextSize(20);
        noticeDate.setGravity(Gravity.CENTER);
        noticeHeader.setLayoutParams(trParams);
        row.addView(noticeDate);

        noticeWriter.setText("글쓴이");
        noticeWriter.setTextSize(20);
        noticeWriter.setGravity(Gravity.CENTER);
        noticeWriter.setLayoutParams(trParams);
        row.addView(noticeWriter);

        itemTable.addView(row);

        while (c.moveToNext()) {
            noticeNumber = new TextView(this);
            noticeHeader = new TextView(this);
            noticeDate = new TextView(this);
            noticeWriter = new TextView(this);

            row = new TableRow(this);
            row.setBackgroundResource(R.drawable.border);
            row.setGravity(Gravity.CENTER);
            row.setOnClickListener(new NoticeView());
            row.setPadding(20, 0, 20, 0);

            noticeNumber.setText(String.valueOf(c.getInt(0)));
            noticeNumber.setTextSize(20);
            noticeNumber.setGravity(Gravity.CENTER);
            row.addView(noticeNumber);

            noticeHeader.setText(c.getString(1));
            noticeHeader.setTextSize(20);
            noticeHeader.setLayoutParams(trParams);
            noticeHeader.setGravity(Gravity.CENTER);
            noticeHeader.setSingleLine(true);
            noticeHeader.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            noticeHeader.setMarqueeRepeatLimit(3);
            noticeHeader.setSelected(true);
            noticeHeader.setWidth(700);
            row.addView(noticeHeader);

            _noticeContent.add(c.getString(2));

            noticeDate.setText(c.getString(3));
            noticeDate.setTextSize(20);
            noticeDate.setLayoutParams(trParams);
            noticeDate.setGravity(Gravity.CENTER);
            noticeDate.setLayoutParams(trParams);
            row.addView(noticeDate);

            noticeWriter.setText("관리자");
            noticeWriter.setTextSize(20);
            noticeWriter.setGravity(Gravity.CENTER);
            row.addView(noticeWriter);

            itemTable.addView(row);
        }
        itemTable.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(925, -1);
        hScrollView.setLayoutParams(lParams);
    }

    @Override
    public void onBackPressed() {
        long temp_t = System.currentTimeMillis();
        long interval_t = temp_t - backPressedTime;

        if ((interval_t >= 0) && (3 * 1000 >= interval_t)) {
            super.onBackPressed();
        } else {
            backPressedTime = temp_t;
            Toast.makeText(this, "한 번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 메뉴 레이아웃 요소를 전부 전개함.
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        // 그 중에서 action_search라는 아이디를 가진 요소를 찾음.
        MenuItem searchItem = menu.findItem(R.id.action_search);
        // SearchView로 넣고 거기에 대한 이벤트 리스너를 등록한다.
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Search(s, "search");
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        MenuItem category = menu.findItem(R.id.category);
        View vCategory = category.getActionView();
        if (vCategory instanceof Spinner) {
            final Spinner spinner = (Spinner) vCategory;
            // 들어갈 adapter 설정
            final ArrayAdapter<CharSequence> adapter =
                    ArrayAdapter.createFromResource(this,
                            R.array.category,
                            android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // 만든 adapter 설정
            spinner.setAdapter(adapter);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    spinner.setPopupBackgroundDrawable(getResources().getDrawable(android.R.color.white, getTheme()));
            }
            // 선택 시 처리할 이벤트 리스너 등록
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // 서치모드 변경
                    searchMode = adapter.getItem(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        return true;
    }

    public void DBerror() {
        Toast.makeText(this, "자식 버튼입니다!!", Toast.LENGTH_SHORT).show();
    }

    private class PictureEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            // 다이얼로그 구성 xml inflate 하기
            layoutInflator = (LayoutInflater) NavActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
            View layout = layoutInflator.inflate(R.layout.detail_view, (ViewGroup) findViewById(R.id.detailView));

            // 상세화면에 표시할 사진 가져오기
            // 클릭한 제품의 실제 id 필요
            String pId = _pictureMap.get(v.getId());

            // 클릭한 제품의 아이디를 가지고 DB에 접속하여 대표사진을 들고 온다.
            Cursor c = DBManager.getInstance(NavActivity.this).database.query(
                    "product",
                    new String[]{"pic"},
                    "id=" + pId,
                    null, null, null, null
            );

            ScrollView detailView = (ScrollView) layout;
            GridLayout imageGrid = (GridLayout) (((HorizontalScrollView) ((LinearLayout) detailView.getChildAt(0)).getChildAt(0)).getChildAt(0));
            imageGrid.setScaleX(0.65f);
            imageGrid.setScaleY(0.65f);
            imageGrid.setPivotX(0);
            imageGrid.setPivotY(0);

            image = (ImageView) ((LinearLayout)imageGrid.getChildAt(0)).getChildAt(0);
            description = (ImageView) ((LinearLayout)imageGrid.getChildAt(1)).getChildAt(0);
            detailImage = (ImageView) ((LinearLayout)imageGrid.getChildAt(2)).getChildAt(0);

            ((LinearLayout)imageGrid.getChildAt(0)).setGravity(Gravity.CENTER);
            ((LinearLayout)imageGrid.getChildAt(1)).setGravity(Gravity.CENTER);
            ((LinearLayout)imageGrid.getChildAt(2)).setGravity(Gravity.CENTER);

            // 일단 이미지 세팅을 전부 끝내고
            while (c.moveToNext()) {
                //ImageView pic = new ImageView(NavActivity.this);
                byte[] picByteData = c.getBlob(0);
                Bitmap newPicture = ByteArrayToBitmap(picByteData);
                newPicture = ResizeBitmap(newPicture, 500);
                if (newPicture != null) {
                    Drawable picDrawable = new BitmapDrawable(getResources(), newPicture);
                    image.setImageDrawable(picDrawable);
                } else {
                    Bitmap textImage = makeBitmapWithText(new String[]{"이미지 없음"}, "", 40f, 200, 50);
                    Drawable picDrawable = new BitmapDrawable(getResources(), textImage);
                    image.setImageDrawable(picDrawable);
                }
            }

            // product 테이블에서 상세 정보 가져오기
            c = DBManager.getInstance(context).database.query("product",
                    //상품설명, 원산지, 브랜드, 규격, 모델, 중량
                    new String[]{"description", "origin", "brand", "standard", "model", "weight"},
                    "id=?",
                    new String[]{pId}, null, null, null);
            c.moveToNext();

            String desc = c.getString(0);
            String origin = c.getString(1);
            String brand = c.getString(2);
            String standard = c.getString(3);
            String model = c.getString(4);
            String weight = c.getString(5);

            String descPlain = /*"설명:" + */desc;
            String originPlain = "원산지:" + origin;
            String brandPlain = "브랜드:" + brand;
            String standardPlain = "규격:" + standard;
            String modelPlain = "모델:" + model;
            String weightPlain = "중량:" + weight;

            Bitmap detailText = makeBitmapWithText(
                    /* 원하는 문자열*/
                    new String[]{
                            descPlain, originPlain, brandPlain, standardPlain, modelPlain, weightPlain
                    },
                    /* 원하는 글꼴 타입*/"",
                    /* 텍스트 크기*/40f,
                    /* 가로 크기*/image.getDrawable().getIntrinsicWidth(),
                    /* 세로 크기*/image.getDrawable().getIntrinsicHeight()
            );
            Drawable detailDrawable = new BitmapDrawable(getResources(), detailText);
            description.setImageDrawable(detailDrawable);

            // picture 테이블에서 상세 사진 가져오기
            c = DBManager.getInstance(context).database.query("picture",
                    new String[]{"pic"},
                    "id=?",
                    new String[]{pId}, null, null, null);
            if (c.moveToNext()) {
                byte[] binaryData = c.getBlob(0);
                Bitmap pic = ByteArrayToBitmap(binaryData);
                if (pic != null) {
                    Drawable picDrawable = new BitmapDrawable(getResources(), pic);
                    detailImage.setImageDrawable(picDrawable);
                }
            } else {
                Bitmap textImage = makeBitmapWithText(new String[]{"이미지 없음"}, "", 40f, 200, 50);
                Drawable picDrawable = new BitmapDrawable(getResources(), textImage);
                detailImage.setImageDrawable(picDrawable);
            }

            // 다이얼로그 생성 및 세팅
            AlertDialog.Builder alert = new AlertDialog.Builder(NavActivity.this);
            alert.setView(layout);
            alert.setIcon(R.drawable.logo);
            alert.setTitle("상품 상세보기");
            alert.setNegativeButton("닫기", null);
            alert.setCancelable(true);

            // 세팅 전부 끝내고 보여주기
            alert.show();
        }
    }

    public Bitmap makeBitmapWithText(String[] _txt, String _fontName, float _text_size, int _width, int _height) {
        Bitmap textBitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888);
        //textBitmap.eraseColor(0x88888888);
        Canvas canvas = new Canvas(textBitmap);
        canvas.drawColor(Color.WHITE);
        Typeface typeface = Typeface.create(_fontName, Typeface.NORMAL);
        Paint textPaint = new Paint();
        textPaint.setTextSize(_text_size);
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(typeface);
        textPaint.setTextAlign(Paint.Align.LEFT);
        Rect bounds = new Rect();

        // 시작점
        float position = 10.0f;
        for (int i = 0; i < _txt.length; i++) {
            textPaint.getTextBounds(_txt[i], 0, _txt[i].length(), bounds);
            canvas.drawText(_txt[i], 0.0f, position + Math.abs(bounds.top - bounds.bottom), textPaint);
            position += textPaint.getTextSize() + 10f;
        }
        return textBitmap;
    }

    // 장바구니 이벤트 리스너
    class AddCart implements View.OnClickListener {

        TableRow r;
        String pid;
        String name;
        String origin;
        String brand;
        String standard;
        String Model;
        String order_price;
        String order_num;
        Bitmap pic_bit;
        ImageView view;
        int number = 0;
        int price = 0;
        boolean isnew = true;

        String temp = "";

        String[] methodList = getResources().getStringArray(R.array.method);

        @Override
        public void onClick(View v) {
            if (itemTable.getChildCount() == 0) {
                Toast.makeText(NavActivity.this, "추가할 상품이 없습니다!", Toast.LENGTH_SHORT).show();
            } else {
                _wholePrice = 0;
                _wholeNumber = 0;
                int count = 0;

                TableRow row = new TableRow(NavActivity.this);

                // 배송방법 선택
                Spinner method = new Spinner(NavActivity.this);
                method.setAdapter(new ArrayAdapter<>(NavActivity.this, android.R.layout.simple_spinner_dropdown_item, methodList));
                // 총 수량
                productWholeNumber = new TextView(NavActivity.this);
                // 공급액 합계
                productWholePrice = new TextView(NavActivity.this);

                // 선택된 상품은 수시로 바뀔 수 있으므로, 매 번 지우고 다시 확인
                productQuantity.clear();
                for (int i = 1; i < itemTable.getChildCount(); i++) {
                    mChildList = new ArrayList<String>();
                    r = (TableRow) itemTable.getChildAt(i);
                    // 해당 상품이 선택되었다면
                    if (((CheckBox)(((LinearLayout) r.getChildAt(r.getChildCount() - 1)).getChildAt(0))).isChecked()) {
                        // 상품의 주문 개수
                        try
                        {
                            number = Integer.parseInt(((EditText)(((LinearLayout)r.getChildAt(r.getChildCount() - 2)).getChildAt(0))).getText().toString());
                            order_num = (((EditText)(((LinearLayout)r.getChildAt(r.getChildCount() - 2)).getChildAt(0))).getText().toString());
                            order_price = (((TextView)(((LinearLayout)r.getChildAt(r.getChildCount() - 3)).getChildAt(0))).getText().toString());
                            Model = (((TextView)(((LinearLayout)r.getChildAt(r.getChildCount() - 4)).getChildAt(0))).getText().toString());
                            standard = (((TextView)(((LinearLayout)r.getChildAt(r.getChildCount() - 5)).getChildAt(0))).getText().toString());
                            brand = (((TextView)(((LinearLayout)r.getChildAt(r.getChildCount() - 6)).getChildAt(0))).getText().toString());
                            origin = (((TextView)(((LinearLayout)r.getChildAt(r.getChildCount() - 7)).getChildAt(0))).getText().toString());
                            name = (((TextView)(((LinearLayout)r.getChildAt(r.getChildCount() - 8)).getChildAt(0))).getText().toString());
                            view = (((ImageView)(((LinearLayout)r.getChildAt(r.getChildCount() - 9)).getChildAt(0))));
                            pic_bit = ((BitmapDrawable)view.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
                            pid = quantityMap.get(((TextView)((LinearLayout) r.getChildAt(r.getChildCount() - 2)).getChildAt(0)).getId());

                            for(int j = 0; j < mGroupList.size(); j++)
                            {
                                if(pid.equals(mGroupList.get(j).get(7)))
                                {
                                    isnew = false;
                                    mGroupList.get(j).set(6, String.valueOf(Integer.parseInt(order_num)));
                                    break;
                                }
                                isnew = true;
                            }
                            if(isnew)
                            {
                                mBitmapList.add(pic_bit);
                                mChildList.add(name);
                                mChildList.add(origin);
                                mChildList.add(brand);
                                mChildList.add(standard);
                                mChildList.add(Model);
                                mChildList.add(order_price);
                                mChildList.add(order_num);
                                mChildList.add(pid);
                                mGroupList.add(mChildList);
                            }
                        }catch (NumberFormatException e)
                        {

                        }


//                        StringTokenizer token = new StringTokenizer(((EditText)((LinearLayout) r.getChildAt(r.getChildCount() - 3)).getChildAt(0)).getText().toString(), ",");
//                        temp = "";
//                        while (token.hasMoreTokens()) {
//                            temp += token.nextToken();
//                        }


                        // 상품 자체의 가격
                        price = Integer.decode(((TextView)((LinearLayout)r.getChildAt(r.getChildCount()-3)).getChildAt(0)).getText().toString());
                        _wholeNumber += number;
                        _wholePrice += number * price;

                        productWholeNumber.setText(String.valueOf(_wholeNumber));
                        productWholePrice.setText(String.valueOf(_wholePrice));

                        // 선택한 상품을 실제 장바구니에 등재
                        // 실제 제품의 아이디 - 제품의 주문 수량0
                        productQuantity.put(quantityMap.get(((TextView)((LinearLayout) r.getChildAt(r.getChildCount() - 2)).getChildAt(0)).getId()), number);

                        count++;
                    }
                }
                if (count == 0) {
                    Toast.makeText(NavActivity.this, "선택된 제품이 없습니다!", Toast.LENGTH_SHORT).show();
                } else {
                    ((TableRow) shoppingCart.getChildAt(0)).removeAllViews();
                    memoRow.removeAllViews();
                    shoppingCart.removeAllViews();

                    TableRow originalRow = new TableRow(NavActivity.this);

                    originalRow.addView(_deliveryMethod);
                    originalRow.addView(t_wholeNumber);
                    originalRow.addView(t_wholePrice);
                    originalRow.addView(_orderButton);

                    // 가장 위에 표시되는 항목
                    shoppingCart.addView(originalRow);

                    TableRow.LayoutParams a = new TableRow.LayoutParams();
                    a.setMargins(0, 0, 53, 0);

                    method.setLayoutParams(a);
                    row.addView(method);

                    productWholeNumber.setLayoutParams(a);
                    row.addView(productWholeNumber);

                    productWholePrice.setLayoutParams(a);
                    row.addView(productWholePrice);

                    // 실제 입력되는 항목들
                    shoppingCart.addView(row);

                    // 메모 항목
                    memoRow.addView(_memo);
                    shoppingCart.addView(memoRow);

                    // 정상적으로 추가됨을 알린다
                    Toast.makeText(NavActivity.this, "정상적으로 추가되었습니다", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }
    private class item_order implements View.OnClickListener{
        @Override
        public void onClick(View v)
        {
            order_Display();
            _pickBtn.setOnClickListener(new delete_items());
            _pickBtn.setText("선택 삭제");
            _orderButton.setText("선택 상품 주문");

        }
    }
    private class delete_items implements View.OnClickListener
    {
        int order_count = 0;
        TableRow r;
        String quantity;
        @Override
        public void onClick(View v)
        {
            _wholeNumber = 0;
            _wholePrice = 0;
            for(order_count = mGroupList.size()-1; order_count >= 0; order_count--)
            {
                    r = (TableRow) itemTable.getChildAt(order_count + 1);
                    // 해당 제품의 수량
                    quantity = (((EditText) (((LinearLayout) r.getChildAt(r.getChildCount() - 2)).getChildAt(0))).getText().toString());
                    _wholeNumber += Integer.parseInt(quantity);
                    _wholePrice += Integer.parseInt(mGroupList.get(order_count).get(5)) * Integer.parseInt(quantity);
            }
            order_count = 0;
            for(order_count = mGroupList.size()-1; order_count >= 0; order_count--)
            {
                r = (TableRow) itemTable.getChildAt(order_count+1);
                if (((CheckBox)(((LinearLayout) r.getChildAt(r.getChildCount() - 1)).getChildAt(0))).isChecked())
                {
                    _wholePrice -= Integer.parseInt(mGroupList.get(order_count).get(5)) * Integer.parseInt(mGroupList.get(order_count).get(6));
                    _wholeNumber -= Integer.parseInt(mGroupList.get(order_count).get(6));
                    mGroupList.remove(order_count);
                }
            }
            productWholePrice.setText(String.valueOf(_wholePrice));
            productWholeNumber.setText(String.valueOf(_wholeNumber));

            order_Display();
            _orderButton.setText("선택 상품 주문");
        }
    }

    private class OrderProduct implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if(mGroupList.size() == 0)
            {
                Toast.makeText(NavActivity.this, "장바구니가 비었습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            String orderContent = "";
            String pId = "";
            String quantity = "";
            String Amount = "";
            int order_count = 0;
            Iterator iterProduct = productQuantity.keySet().iterator();
            TableRow r = (TableRow) ((TableLayout) ((TableRow) v.getParent()).getParent()).getChildAt(1);

            String shopName = "";
            String rep = "";
            String addr = "";
            String phone = "";
            String cellPhone = "";
            String freight = "";
            String fax = "";
            String selectedMethod = "";
            String memoText = _memo.getText().toString().equals("") ? "없음" : _memo.getText().toString();

            // 현재시간을 ms 단위로 구함
            long now = System.currentTimeMillis();

            // 일자 형식으로 변환
            Date today = new Date(now);

            // 시간 포맷 지정
            SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // 지정된 포맷을 사용하여 String 타입으로 변환
            String todayDate = DateFormat.format(today);

            SQLiteDatabase db = DBManager.getInstance(context).database;

            // 내부 DB에서 정보 가져옴
            Cursor c = db.query("user",
                    new String[]{"shopname", "representative", "address", "phone", "cellphone", "freight", "fax"},
                    "id=" + _id,
                    null, null, null, null);

            // 받아온 데이터 저장
            while (c.moveToNext()) {
                shopName = c.getString(0);
                rep = c.getString(1);
                addr = c.getString(2);
                phone = c.getString(3);
                cellPhone = c.getString(4);
                freight = c.getString(5);
                fax = c.getString(6);
            }
            c.close();

            // 선택한 배달방법 데이터 획득
            String method = ((Spinner) r.getChildAt(0)).getSelectedItem().toString();
            switch (method) {
                case METHOD_FREIGHT:
                    selectedMethod = METHOD_FREIGHT;
                    break;
                case METHOD_PARCEL:
                    selectedMethod = METHOD_PARCEL;
                    break;
                case METHOD_MANUALLY:
                    selectedMethod = METHOD_MANUALLY;
                    break;
                default:
                    Toast.makeText(NavActivity.this, "잘못된 선택입니다.", Toast.LENGTH_SHORT).show();
            }
            _wholeNumber = 0;
            _wholePrice = 0;
            order_count = 0;
            while (order_count != mGroupList.size()) {
                r = (TableRow) itemTable.getChildAt(order_count + 1);
                if (((CheckBox)(((LinearLayout) r.getChildAt(r.getChildCount() - 1)).getChildAt(0))).isChecked())
                {
                    r = (TableRow) itemTable.getChildAt(order_count + 1);
                    // 해당 제품의 수량
                    quantity = (((EditText) (((LinearLayout) r.getChildAt(r.getChildCount() - 2)).getChildAt(0))).getText().toString());
                    _wholeNumber += Integer.parseInt(quantity);
                    _wholePrice += Integer.parseInt(mGroupList.get(order_count).get(5)) * Integer.parseInt(quantity);

                }
                order_count++;
            }
            order_count = 0;
            String query = "INSERT INTO orders (shopname, representative, address, phone, cellphone, memo, sum, delivery, amount, date, id, fax, freight, ordernum) ";

            /** 내부 DB 업데이트 **/
            // ordernum과 confirm은 제외한다. ordernum은 Auto-Increase 이고 confirm은 추가 승인이 필요하므로.
            db.execSQL(
                    "INSERT INTO orders (shopname, representative, address, phone, cellphone, memo, sum, delivery, amount, date, id, fax, freight) " +
                            String.format("VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');", shopName, rep, addr, phone, cellPhone, _memo.getText().toString(),
                                    _wholePrice, selectedMethod, _wholeNumber, todayDate, _id, fax, freight));

            // 가져와야 할 주문번호: 오늘(xxxx년xx월xx일 - todayDate) ~~~라고 메모를 남긴(_memo.getText().toString()) xxx가(홍길동 - _id) 한 주문
            c = db.query("orders",
                    new String[]{"ordernum"},
                    "id=? and memo=? and date=?",
                    new String[]{_id, _memo.getText().toString(), todayDate},
                    null,
                    null,
                    null
            );

            String orderNumber = "";
            /**
             * 주문을 할 떄마다 주문 번호가 늘어나므로, 모든 사항(누가, 언제, 메모)이 일치하더라도
             * 가장 나중에 등록된 주문번호를 들고 올 수 있도록 반복문을 실행한다. 그렇지 않으면
             * 가장 처음에 주문한 내역에 모두 쌓이게 될것이다.
             * */
            while(c.moveToNext()) {
                // 주문번호 획득
                 orderNumber = c.getString(0);
            }

            query += String.format("VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s', '%s');", shopName, rep, addr, phone, cellPhone, _memo.getText().toString(),
                    _wholePrice, selectedMethod, _wholeNumber, todayDate, _id, fax, freight, orderNumber);

            String query2 = "insert into detailorder (name, origin, brand, standard, model, cost, amount, ordernum) VALUES";

            // 가져온 주문번호를 가지고 개별적인 주문을 처리
            while (order_count != mGroupList.size()) {
                if (((CheckBox)(((LinearLayout) r.getChildAt(r.getChildCount() - 1)).getChildAt(0))).isChecked())
                {
                    r = (TableRow) itemTable.getChildAt(order_count + 1);
                    // 제품 id
                    pId = mGroupList.get(order_count).get(7).toString();

                    // 해당 제품의 수량
                    quantity = (((EditText) (((LinearLayout) r.getChildAt(r.getChildCount() - 2)).getChildAt(0))).getText().toString());
                    mGroupList.get(order_count).set(6, quantity);
                    Amount = String.valueOf(Integer.parseInt(mGroupList.get(order_count).get(5)) * Integer.parseInt(quantity));


                    c = db.query("product", new String[]{"name", "origin", "brand", "standard", "model", "cost"},
                            "id=" + pId,
                            null, null, null, null
                    );

                    c.moveToNext();
                    // detailorder에 개별 주문 정보를 update
                    String standard = c.getString(3);
                    if(standard.charAt(standard.length()-1) == '\'' && standard.charAt(standard.length()-2) != '\''){
                        standard = new StringBuilder(standard).deleteCharAt(standard.length()-1).append('"').toString();
                    }else if (standard.charAt(standard.length()-1) == '\'' && standard.charAt(standard.length()-2) == '\''){
                        standard = new StringBuilder(standard).deleteCharAt(standard.length()-1).toString();
                        standard = new StringBuilder(standard).deleteCharAt(standard.length()-1).append('"').toString();
                    }
                    db.execSQL("insert into detailorder (name, origin, brand, standard, model, cost, amount, ordernum) " +
                            String.format("VALUES ('%s','%s','%s','%s','%s','%s','%s','%s');"
                                    , c.getString(0), c.getString(1), c.getString(2), standard, c.getString(4), Amount, quantity, orderNumber));

                    query2 += String.format(" ('%s','%s','%s','%s','%s','%s','%s','%s'),",
                            c.getString(0), c.getString(1), c.getString(2), standard, c.getString(4), Amount, quantity, orderNumber);
                }
                order_count++;
            }
            char[] t = query2.toCharArray();
            t[query2.length() - 1] = ';';
            query2 = String.valueOf(t);
            /** 내부 DB 업데이트 종료 **/

            /** 서버에 업데이트 내용 전달 **/
            String updateContent = "orders=" + query + "&detailorder=" + query2;

            new Update(MODE_UPDATE, orderNumber).execute(updateContent);
        }
    }

    // 공지사항 상세보기
    private class NoticeView implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // 이 activity와 연결되지 않은 다른 XML 문서로부터 받아오는 것이므로 직접 inflate해야 한다
            layoutInflator = (LayoutInflater) NavActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);

            View root = layoutInflator.inflate(R.layout.notice_view, null);
            View notice = ((ScrollView) root).getChildAt(0);
            GridLayout noticeGrid = (GridLayout) ((LinearLayout) notice).getChildAt(0);

            TextView noticeHeader = (TextView) (noticeGrid).getChildAt(1);
            TextView noticeContent = (TextView) (noticeGrid).getChildAt(3);

            TableRow selectedRow = (TableRow)v;
            TableLayout parent = (TableLayout)((TableRow)v).getParent();

            int length = parent.getChildCount();
            int index = 0;

//            for(int i = 1; i < length; i++){
//                if(parent.getChildAt(i).getId() == selectedRow.getId()){
//                    index = i;
//                    break;
//                }
//            }

            noticeHeader.setText(((TextView) ((TableRow) v).getChildAt(1)).getText().toString());
//            noticeContent.setText(_noticeContent.get(_noticeContent.size() - 2 - index));
            noticeContent.setText(_noticeContent.get(parent.indexOfChild(v)-1));
            noticeContent.setGravity(Gravity.CENTER);
            noticeContent.setWidth(500);

            // 다이얼로그 생성 및 세팅
            AlertDialog.Builder alert = new AlertDialog.Builder(NavActivity.this);
            alert.setView(root);
            alert.setIcon(R.drawable.logo);
            alert.setTitle("공지사항");
            alert.setNegativeButton("닫기", null);
            alert.setCancelable(true);
            alert.show();
        }
    }

    private class Update extends AsyncTask<String, String, String>{
        ProgressDialog pDialog;
        int mode = 0;
        Order order = ((NavActivity)NavActivity.context).order;
        Inquiry inquiry = ((NavActivity)NavActivity.context).inquiry;
        GetDraft draft = ((NavActivity)NavActivity.context).draft;
        private String[] personInfo = new String[7];
        private String[] orderInfo = new String[6];
        private String regitNumber = new String("");
        private ArrayList<String> productName = new ArrayList<>();
        private ArrayList<String> productOrigin = new ArrayList<>();
        private ArrayList<String> productBrand= new ArrayList<>();
        private ArrayList<String> productStandard = new ArrayList<>();
        private ArrayList<String> productModel = new ArrayList<>();
        private ArrayList<String> productCost= new ArrayList<>();
        private ArrayList<String> productQuantity = new ArrayList<>();

        GridLayout.LayoutParams lParams = new GridLayout.LayoutParams();

        int count = 0;
        String ordernum = "";

        private JSONObject changedUserData;
        private JSONObject result;

        Cursor c;
        Change change;

        public Update(int mode) {
            this.mode = mode;
        }

        public Update(int mode, String ordernum) {
            this.mode = mode;
            this.ordernum = ordernum;
        }

        public void SetUserData(JSONObject data){
            this.changedUserData = data;
        }

        @Override
        protected void onPreExecute() {
            if (mode == MODE_UPDATE) {
                pDialog = ProgressDialog.show(
                /* 표시할 액티비티의 context*/ NavActivity.context,
                /* 다이얼로그 제목 문자열 */ "주문",
                /* 다이얼로그 내용 문자열 */ "진행중...",
                /* 진행여부를 알 수 있는지 유뮤. true가 알 수 없음, false가 알 수 있음 */ true,
                /* 취소 가능 여부. 기본값 false -> 이전 키 및 바깥 영역 클릭해도 취소되지 않음 */ true
                /* 다이얼로그가 취소를 알리는 리스너 -> cancelListener*/
                );
            }
            else if (mode == MODE_CONFIRM_CHECK){
                pDialog = ProgressDialog.show(
                        NavActivity.context,
                        "주문 확인",
                        "조회중...",
                        true,
                        true
                );
            }
            else if (mode == MODE_USER_INFO_CHANGE){
                change = new Change();
                pDialog = ProgressDialog.show(
                        NavActivity.context,
                        "회원 정보 변경",
                        "변경중...",
                        true,
                        true
                );
            }
            else if (mode == MODE_NOTICE_UPDATE){
                change = new Change();
                pDialog = ProgressDialog.show(
                        NavActivity.context,
                        "공지사항 확인",
                        "확인중...",
                        true,
                        true
                );
            }
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if(mode==MODE_UPDATE) {
                    return order.request(params[0]);
                }
                else if(mode==MODE_CONFIRM_CHECK){
                    inquiry.setOrderNumber(params[0]);
                    Boolean result = (Boolean)inquiry.request(this.mode);
                    if(result){
                        count = 0;
                        draft.setOrderNumber(params[0]);
                        JSONArray arr = draft.request(MODE_ORDER_DRAFT);
                        String picData = "";
                        // 발주서가 준비되지 않은 경우
                        if(arr.length() == 0){
                            c = DBManager.getInstance(context).database.query("orders",
                                    new String[]{"shopname, representative, address, phone, cellphone, memo, sum, delivery, amount, date, fax, freight"},
                                    String.format("ordernum='%s'",params[0]),
                                    null, null, null, null
                            );
                            c.moveToNext();
                            // 상호명
                            personInfo[0] = c.getString(0);
                            // 대표자 성함
                            personInfo[1] = c.getString(1);
                            // 주소
                            personInfo[2] = c.getString(2);
                            // 전화번호
                            personInfo[3] = c.getString(3);
                            // 휴대전화
                            personInfo[4] = c.getString(4);
                            // 팩스번호
                            personInfo[5] = c.getString(10);
                            // 화물지점
                            personInfo[6] = c.getString(11);

                            // 메모
                            orderInfo[0] = c.getString(5);
                            // 총 합계
                            orderInfo[1] = c.getString(6);
                            // 배달방법
                            orderInfo[2] = c.getString(7);
                            // 총 주문수량
                            orderInfo[3] = c.getString(8);
                            // 주문일자
                            orderInfo[4] = c.getString(9);
                            // 주문번호
                            orderInfo[5] = params[0];

                            c = DBManager.getInstance(context).database.query("detailorder",
                                    new String[]{"name", "origin", "standard", "brand", "model", "cost", "amount"},
                                    String.format("ordernum='%s'", orderInfo[5]),
                                    null, null, null, null
                            );

                            while(c.moveToNext()){
                                productName.add(c.getString(0));
                                productOrigin.add(c.getString(1));
                                productBrand.add(c.getString(2));
                                productStandard.add(c.getString(3));
                                productModel.add(c.getString(4));
                                productCost.add(c.getString(5));
                                productQuantity.add(c.getString(6));
                                count++;
                            }

                        }else {
                            for (int i = 0; i < arr.length(); i++) {
                                picData += arr.getString(i);
                                if (i < arr.length()) {
                                    picData += ",";
                                }
                            }
                        }
                        return picData;
                    }
                    else{
                        return "failed";
                    }
                }
                else if (mode == MODE_USER_INFO_CHANGE){
                    String id = this.changedUserData.getString("id");
                    String pw = this.changedUserData.getString("pw");
                    String shopname = this.changedUserData.getString("shopname");
                    String representative = this.changedUserData.getString("representative");
                    String address = this.changedUserData.getString("address");
                    String phone = this.changedUserData.getString("phone");
                    String cellPhone = this.changedUserData.getString("cellphone");
                    String freight = this.changedUserData.getString("freight");
                    String fax = this.changedUserData.getString("fax");
                    String regit = this.changedUserData.getString("regitnum");
                    String action = "change";

                    String parameter = "id="+id+"&pw="+pw+"&shopname="+shopname+"&representative="+
                            representative+"&address="+address+"&phone="+phone+"&cellphone="+cellPhone+
                            "&freight="+freight+"&fax="+fax+"&regitnum="+regit+
                            "&action="+action;

                    return change.request(parameter);
                }
                else if (mode == MODE_NOTICE_UPDATE){
                    String parameter = "action="+ "notice";
                    DBManager.getInstance(context).database.delete("notice",
                            null, null);
                    String r = change.request(parameter);
                    if(r.equals("fail")){
                        return r;
                    }
                    else if(r.equals("success")){
                        return "renew_success";
                    }
                }
            } catch (IOException e) {
                return "fail";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();
            if(s.equals("fail")){
                Toast.makeText(NavActivity.context, "조회 실패! 다시 시도하시기 바랍니다.", Toast.LENGTH_SHORT).show();
            }
            // 회원정보 수정을 처리하기 위한 부분
            else if (s.equals("success")){
                _userData = changedUserData;
                Toast.makeText(context, "변경 성공!", Toast.LENGTH_SHORT).show();
            }// 주문 성공
            else if (s.equals("order_success")){
                Toast.makeText(context, "주문 성공!", Toast.LENGTH_SHORT).show();
            }
            // 갱신 완료
            else if (s.equals("renew_success")){
                Toast.makeText(context, "갱신을 완료했습니다.", Toast.LENGTH_SHORT).show();
                c = DBManager.getInstance(context).database.query("notice",
                        new String[]{
                                "number", "header", "content", "date"
                        },
                        null, null, null, null,
                        "number desc");
                DisplayNotice(c);
            }
            // 주문 실패 시
            else if (s.equals("order_fail")){
                int r = 0;
                Toast.makeText(context, "주문 실패", Toast.LENGTH_SHORT).show();
                try {
                    r = DBManager.getInstance(context).database.delete("orders",
                            String.format("ordernum='%s'", this.ordernum),
                            null);
                    r = DBManager.getInstance(context).database.delete("detailorder",
                            String.format("ordernum='%s'", this.ordernum),
                            null);
                }
                catch(SQLException e){
                    e.printStackTrace();
                }
            }
            // 업데이트 실패 시
            else if (s.equals("update_fail")){
                Toast.makeText(context, "변경 실패!", Toast.LENGTH_SHORT).show();
            }
            // 발주서 BASE64 인코딩 String 데이터가 들어옴
            else{
                String[] data = s.split(",");

                layoutInflator = (LayoutInflater)NavActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View root = layoutInflator.inflate(R.layout.notice_view, null);

                // 표 객체의 root에 해당하는 레이아웃
                LinearLayout draftLayout = (LinearLayout)View.inflate(context, R.layout.order_draft, null);

                // 주문 정보에 대한 레이아웃
                LinearLayout orderLayout =(LinearLayout)((LinearLayout)draftLayout.getChildAt(1)).getChildAt(0);
                // 주문 내역에 대한 레이아웃
                GridLayout orderItem = (GridLayout)((LinearLayout)draftLayout.getChildAt(2)).getChildAt(0);
                // 기타 사항에 대한 레이아웃
                LinearLayout etc =  (LinearLayout)((LinearLayout) draftLayout.getChildAt(3)).getChildAt(0);

                /**
                 * 임의 발주서에 표시
                 * */

                // 배송방법
                TextView method = (TextView)((LinearLayout)((LinearLayout)orderLayout.getChildAt(0)).getChildAt(1)).getChildAt(0);
                method.setText(orderInfo[2]);

                // 주문날짜
                TextView orderDate = (TextView) ((LinearLayout)((LinearLayout)orderLayout.getChildAt(0)).getChildAt(3)).getChildAt(0);
                orderDate.setText(orderInfo[4]);

                // 등록번호
                TextView regitNumber =
                        (TextView)((LinearLayout)((LinearLayout)((GridLayout)((LinearLayout)
                                orderLayout.getChildAt(2)).getChildAt(0)).getChildAt(0)).getChildAt(1)).getChildAt(0);
                try {
                    regitNumber.setText(_userData.getString("regitnum").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 상호
                TextView shop =
                        (TextView)((LinearLayout)((LinearLayout)((GridLayout)((LinearLayout)
                                orderLayout.getChildAt(2)).getChildAt(0)).getChildAt(1)).getChildAt(1)).getChildAt(0);
                shop.setText(personInfo[0]);

                // 성명
                TextView name =
                        (TextView)((LinearLayout)((LinearLayout)((GridLayout)((LinearLayout)
                                orderLayout.getChildAt(2)).getChildAt(0)).getChildAt(1)).getChildAt(3)).getChildAt(0);
                name.setText(personInfo[1]);

                // 주소
                TextView addr =
                        (TextView)((LinearLayout)((LinearLayout)((GridLayout)((LinearLayout)
                                orderLayout.getChildAt(2)).getChildAt(0)).getChildAt(2)).getChildAt(1)).getChildAt(0);
                addr.setText(personInfo[2]);

                // TEL
                TextView tel =
                        (TextView)((LinearLayout)((LinearLayout)((GridLayout)((LinearLayout)
                                orderLayout.getChildAt(2)).getChildAt(1)).getChildAt(1)).getChildAt(0)).getChildAt(0);
                tel.setText(personInfo[3]);

                // 휴대전화
                TextView cellPhone =
                        (TextView)((LinearLayout)((LinearLayout)((GridLayout)((LinearLayout)
                                orderLayout.getChildAt(2)).getChildAt(1)).getChildAt(1)).getChildAt(1)).getChildAt(0);
                cellPhone.setText(personInfo[4]);

                // 화물지점
                TextView freight =
                        (TextView)((LinearLayout)((LinearLayout)((LinearLayout)
                                orderLayout.getChildAt(2)).getChildAt(2)).getChildAt(1)).getChildAt(0);
                freight.setText(personInfo[6]);

                // FAX
                TextView fax =
                        (TextView)((LinearLayout)((LinearLayout)((LinearLayout)
                                orderLayout.getChildAt(2)).getChildAt(2)).getChildAt(3)).getChildAt(0);
                fax.setText(personInfo[5]);

                // 총수량
                TextView quantity = (TextView) ((LinearLayout)((LinearLayout)etc.getChildAt(0)).getChildAt(1)).getChildAt(0);
                quantity.setText(orderInfo[3]);

                // 공급합계
                TextView sum = (TextView)((LinearLayout)((LinearLayout)etc.getChildAt(0)).getChildAt(3)).getChildAt(0);
                sum.setText(orderInfo[1]);

                // 메모
                TextView memo = (TextView) ((LinearLayout)((LinearLayout)etc.getChildAt(1)).getChildAt(1)).getChildAt(0);
                memo.setText(orderInfo[0]);

                /* 새부 항목들을 추가하는 부분 */
                for(int i = 0; i < count; i++) {
                    LinearLayout product = new LinearLayout(context);
                    product.setPadding(5, 5, 5, 5);
                    product.setBackgroundResource(R.drawable.border);
                    TextView text = new TextView(context);
                    text.setText(productName.get(i));
                    orderItem.addView(product);
                    product.addView(text);
                    lParams = new GridLayout.LayoutParams(GridLayout.spec(i+1), GridLayout.spec(0));
                    lParams.setGravity(Gravity.FILL_HORIZONTAL);
                    product.setLayoutParams(lParams);

                    product = new LinearLayout(context);
                    product.setPadding(5, 5, 5, 5);
                    product.setBackgroundResource(R.drawable.border);
                    text = new TextView(context);
                    text.setText(productOrigin.get(i));
                    orderItem.addView(product);
                    product.addView(text);
                    lParams = new GridLayout.LayoutParams(GridLayout.spec(i+1), GridLayout.spec(1));
                    lParams.setGravity(Gravity.FILL_HORIZONTAL);
                    product.setLayoutParams(lParams);

                    product = new LinearLayout(context);
                    product.setPadding(5, 5, 5, 5);
                    product.setBackgroundResource(R.drawable.border);
                    text = new TextView(context);
                    text.setText(productBrand.get(i));
                    orderItem.addView(product);
                    product.addView(text);
                    lParams = new GridLayout.LayoutParams(GridLayout.spec(i+1), GridLayout.spec(2));
                    lParams.setGravity(Gravity.FILL_HORIZONTAL);
                    product.setLayoutParams(lParams);

                    product = new LinearLayout(context);
                    product.setPadding(5, 5, 5, 5);
                    product.setBackgroundResource(R.drawable.border);
                    text = new TextView(context);
                    text.setText(productStandard.get(i));
                    orderItem.addView(product);
                    product.addView(text);
                    lParams = new GridLayout.LayoutParams(GridLayout.spec(i+1), GridLayout.spec(3));
                    lParams.setGravity(Gravity.FILL_HORIZONTAL);
                    product.setLayoutParams(lParams);

                    product = new LinearLayout(context);
                    product.setPadding(5, 5, 5, 5);
                    product.setBackgroundResource(R.drawable.border);
                    text = new TextView(context);
                    text.setText(productModel.get(i));
                    orderItem.addView(product);
                    product.addView(text);
                    lParams = new GridLayout.LayoutParams(GridLayout.spec(i+1), GridLayout.spec(4));
                    lParams.setGravity(Gravity.FILL_HORIZONTAL);
                    product.setLayoutParams(lParams);

                    product = new LinearLayout(context);
                    product.setPadding(5, 5, 5, 5);
                    product.setBackgroundResource(R.drawable.border);
                    text = new TextView(context);
                    text.setText(productCost.get(i));
                    orderItem.addView(product);
                    product.addView(text);
                    lParams = new GridLayout.LayoutParams(GridLayout.spec(i+1), GridLayout.spec(5));
                    lParams.setGravity(Gravity.FILL_HORIZONTAL);
                    product.setLayoutParams(lParams);

                    product = new LinearLayout(context);
                    product.setPadding(5, 5, 5, 5);
                    product.setBackgroundResource(R.drawable.border);
                    text = new TextView(context);
                    text.setText(productQuantity.get(i));
                    orderItem.addView(product);
                    product.addView(text);
                    lParams = new GridLayout.LayoutParams(GridLayout.spec(i+1), GridLayout.spec(6));
                    lParams.setGravity(Gravity.FILL_HORIZONTAL);
                    product.setLayoutParams(lParams);
                }

                AlertDialog.Builder alert = new AlertDialog.Builder(NavActivity.this);

                TableRow.LayoutParams trParams = new TableRow.LayoutParams();
                trParams.setMargins(0, 10, 0, 20);

                LinearLayout lLayout = new LinearLayout(context);
                lLayout.setOrientation(LinearLayout.VERTICAL);

                if(s.equals("")) {
                    ImageView draftImage = new ImageView(context);
                    lLayout.addView(draftImage);
                    draftLayout.setScaleX(0.7f);
                    draftLayout.setScaleY(0.7f);
                    lLayout.addView(draftLayout);

                    Point pt = new Point();
                    String text = "아직 관리자가 확인하지 않았습니다.";
                    Bitmap textImage= makeBitmapWithText(new String[]{text}, "", 40f, 600, 50);
                    Drawable picDrawable = new BitmapDrawable(getResources(), textImage);
                    draftImage.setImageDrawable(picDrawable);
                    draftImage.setLayoutParams(trParams);
                }
                else{
                    for(int i = 0; i < data.length; i++){

                        ImageView draftImage = new ImageView(context);
                        lLayout.addView(draftImage);

                        Point pt = new Point();
                        String text = "주문이 확인되었습니다.";
                        Bitmap textImage= makeBitmapWithText(new String[]{text}, "", 40f, 600, 50);
                        Drawable picDrawable = new BitmapDrawable(getResources(), textImage);
                        draftImage.setImageDrawable(picDrawable);
                        draftImage.setLayoutParams(trParams);

                        Drawable picData = new BitmapDrawable(getResources(), ByteArrayToBitmap(Base64.decode(data[i], 0)));
                        ImageView draft = new ImageView(context);
                        draft.setImageDrawable(picData);
                        lLayout.addView(draft);
                    }
                }
                ScrollView sv = new ScrollView(context);
                sv.addView(lLayout);

                alert.setView(sv);
                alert.setIcon(R.drawable.logo);
                alert.setTitle("주문서 상세내역");
                alert.setNegativeButton("닫기", null);
                alert.setCancelable(true);
                alert.show();
            }
            super.onPostExecute(s);
        }
    }

    private class Order extends NavActivity{
        // 접속대상 서버의 주소를 가진 객체
        private URL url;
        //통신을 담당하는 객체
        private HttpURLConnection conn;

        // 필요한 객체 초기화
        public Order(){
            try{
                url =new URL("http://36.39.144.65:8084/order.jsp");
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public String request(String parameter) throws IOException{
            String params = parameter;
            String parsedData = "";
            InputStream is = null;
            BufferedReader buff = null;

            conn = (HttpURLConnection)url.openConnection();

            // 보내는 데이터를 설정
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 받는 데이터를 JSON 타입으로 설정
            conn.setRequestProperty("Accept", "application/json");

            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.flush();
            os.write(params.getBytes("UTF-8"));

            //웹 서버에 요청하는 시점
            conn.connect();

            try {
                // 웹 서버로부터 전송받을 데이터에 대한 스트림 얻기
                is = conn.getInputStream();

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
                conn.disconnect();
                return parsedData;
            }
            catch(SocketTimeoutException e){
                e.getStackTrace();
                return "order_fail";
            }
        }
    }

    private class Inquiry extends NavActivity {
        // 접속대상 서버의 주소를 가진 객체
        private URL url;
        //통신을 담당하는 객체
        private HttpURLConnection conn;
        private String orderNumber = "";
        private String parsedData = "";

        // 필요한 객체 초기화
        public Inquiry() {
            try {
                url = new URL("http://36.39.144.65:8084/inquiry.jsp");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setOrderNumber(String number) {
            this.orderNumber = number;
        }

        public Object request(int modeParam) throws IOException {
            int mode = modeParam;
            String params = "mode=" + String.valueOf(mode) + "&ordernum=" + orderNumber;
            HashMap<String, byte[]> data = new HashMap<>();
            conn = (HttpURLConnection) url.openConnection();

            // 보내는 데이터를 설정
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 받는 데이터를 JSON 타입으로 설정
            conn.setRequestProperty("Accept", "application/json");

            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.flush();
            os.write(params.getBytes("UTF-8"));

            //웹 서버에 요청하는 시점
            conn.connect();
            // 웹 서버로부터 전송받을 데이터에 대한 스트림 얻기
            InputStream is = conn.getInputStream();

            BufferedReader buff = new BufferedReader(new InputStreamReader(is));
            StringBuffer str = new StringBuffer();
            String d = null;

            while ((d = buff.readLine()) != null) {
                str.append(d);
            }

            // HTML 구조를 파싱하는 부분
            Document doc = Jsoup.parse(str.toString());

            // 갖고 온 전체 HTML DOM에서 필요한 부분을 추출한다.
            Elements resultData = doc.select("body");

            for (Element r : resultData) {
                parsedData = r.text();
            }

            if (modeParam == MODE_CONFIRM_CHECK) {
                if (parsedData.equals("success")) {
                    return true;
                }
            }
            return false;
        }
    }

    private class GetDraft extends NavActivity {
        // 접속대상 서버의 주소를 가진 객체
        private URL url;
        //통신을 담당하는 객체
        private HttpURLConnection conn;
        private String orderNumber = "";
        private String parsedData = "";

        public GetDraft() {
            try {
                url = new URL("http://36.39.144.65:8084/inquiry.jsp");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setOrderNumber(String number) {
            this.orderNumber = number;
        }

        public JSONArray request(int modeParam) throws IOException, JSONException {
            int mode = modeParam;
            String params = "mode=" + String.valueOf(mode) + "&ordernum=" + orderNumber;
            conn = (HttpURLConnection) url.openConnection();

            // 보내는 데이터를 설정
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 받는 데이터를 JSON 타입으로 설정
            conn.setRequestProperty("Accept", "application/json");

            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.flush();
            os.write(params.getBytes("UTF-8"));

            //웹 서버에 요청하는 시점
            conn.connect();
            // 웹 서버로부터 전송받을 데이터에 대한 스트림 얻기
            InputStream is = conn.getInputStream();

            BufferedReader buff = new BufferedReader(new InputStreamReader(is));
            StringBuffer str = new StringBuffer();
            String d = null;

            while ((d = buff.readLine()) != null) {
                str.append(d);
            }

            // HTML 구조를 파싱하는 부분
            Document doc = Jsoup.parse(str.toString());

            // 갖고 온 전체 HTML DOM에서 필요한 부분을 추출한다.
            Elements resultData = doc.select("body");

            for (Element r : resultData) {
                parsedData = r.text();
            }

            JSONObject jsonObject = new JSONObject(parsedData);
            JSONArray jsonResult = jsonObject.getJSONArray("draft");
            return jsonResult;
            //return Base64.decode(jsonResult.getString("draft"), 0);
        }
    }

    private class Change extends NavActivity{
        // 접속대상 서버의 주소를 가진 객체
        private URL url;
        //통신을 담당하는 객체
        private HttpURLConnection conn;

        // 필요한 객체 초기화
        public Change(){
            try{
                url = new URL("http://36.39.144.65:8084/index.jsp");
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public String request(String parameter) throws IOException {
            String params = parameter;
            String parsedData = "";
            BufferedReader buff;

            conn = (HttpURLConnection)url.openConnection();

            // 보내는 데이터를 설정
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 받는 데이터를 JSON 타입으로 설정
            conn.setRequestProperty("Accept", "application/json");

            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.flush();
            os.write(params.getBytes("UTF-8"));

            //웹 서버에 요청하는 시점
            conn.connect();
            // 웹 서버로부터 전송받을 데이터에 대한 스트림 얻기
            InputStream is = conn.getInputStream();

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

            JSONObject json = null;
            SQLiteDatabase db = DBManager.getInstance(context).database;
            ContentValues conv = new ContentValues();

            if(parameter.contains("notice")){
                try {
                    JSONObject obj = new JSONObject(parsedData);
                    JSONArray a = obj.getJSONArray("data");
                    for(int i = 0; i< a.length(); i++) {
                        JSONArray arr = (JSONArray) a.get(i);
                        conv.put("number", arr.getInt(0));
                        conv.put("header", arr.getString(1));
                        conv.put("content", arr.getString(2));
                        conv.put("date", arr.getString(3));
                        db.insert("notice", null, conv);
                    }
                    return "success";
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "fail";
                }
            }
            else {
                try {
                    json = new JSONObject(parsedData);
                    JSONArray jArray = json.getJSONArray("Result");

                    json = jArray.getJSONObject(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                conn.disconnect();

                try {
                    if (json.getString("update").equals("success"))
                        return "success";
                    else {
                        return "update_fail";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return "";
        }
    }

}

class IdGen {
    final static private AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    static public int _generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
}