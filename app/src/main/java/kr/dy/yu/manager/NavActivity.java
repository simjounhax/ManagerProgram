package kr.dy.yu.manager;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class NavActivity extends AppCompatActivity implements View.OnClickListener, ActionBar.OnNavigationListener {
    LinearLayout listLayout;
    String[] parentItem = new String[]{
            "공구", "철물", "캠핑/사무", "생활/잡화", "랜턴", "칼", "신제품", "전체상품", "공지사항"
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

    TextView _picture;
    TextView _productName;
    TextView _origin;
    TextView _brand;
    TextView _standard;
    TextView _model;
    TextView _cost;
    Button _pickBtn;

    ImageView iv;

    TableLayout itemTable;
    TableRow firstRow;

    // 장바구니
    TableLayout shoppingCart;

    public static Context context;
    public Order order;

    int rowPosition = 0;
    int colPosition = 0;

    int parentPosition = 0;
    int childPosition = 0;

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

    public Bitmap ByteArrayToBitmap(byte[] picData){
        Bitmap bitmap = BitmapFactory.decodeByteArray(/*데이터 소스*/picData, /*시작점*/0,
                                      /*Decode할 길이*/picData.length);
        return bitmap;
    }

    public Bitmap ResizeBitmap(Bitmap bitmap, float wantHeight){
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        if(height > wantHeight){
            float percent = (float)(height/100);
            float scale = (float)(wantHeight/percent);
            width *= (scale/100);
            height *= (scale/100);
        }

        return Bitmap.createScaledBitmap(bitmap, (int)width, (int)height, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preData = getIntent();
        _id = preData.getStringExtra("id");

        order = new Order();

        setContentView(R.layout.activity_nav);

        _picture = (TextView)findViewById(R.id.picture);
        _productName = (TextView)findViewById(R.id.productName);
        _origin = (TextView)findViewById(R.id.origin);
        _brand = (TextView)findViewById(R.id.brand);
        _standard = (TextView)findViewById(R.id.standard);
        _model = (TextView)findViewById(R.id.model);
        _cost = (TextView)findViewById(R.id.cost);
        _pickBtn = (Button)findViewById(R.id.pickBtn);

        _pickBtn.setOnClickListener(new AddCart());

        context = this;

        itemTable = (TableLayout) findViewById(R.id.itemTable);
        shoppingCart = (TableLayout) findViewById(R.id.shoppingCart);

        firstRow = (TableRow)findViewById(R.id.firstRow);
        memoRow = (TableRow)findViewById(R.id.belowSecondRow);

        _memo = (EditText)memoRow.getChildAt(0);

        _deliveryMethod = (TextView)findViewById(R.id.deliveryNethod);
        t_wholeNumber = (TextView)findViewById(R.id.wholeNumber);
        t_wholePrice = (TextView)findViewById(R.id.wholePrice);
        _orderButton = (Button)findViewById(R.id.orderButton);
        _orderButton.setOnClickListener(new OrderProduct());

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
            listLayout.addView(parentButton[i], i);

            // 끝에서 첫번째(전체상품)와 두번째(신제품)만 다른 클릭 이벤트 리스너 등록
            if (i == parentItem.length - 1 || i == parentItem.length - 2) {
                parentButton[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 신제품
                        if(((Button)v).getText().equals(parentItem[parentItem.length-3])){
                            Search("new", "");
                        }

                        // 전체 상품
                        if(((Button)v).getText().equals(parentItem[parentItem.length-2])){
                            Search("all", "");
                        }

                        // 공지 사항
                        if(((Button)v).getText().equals(parentItem[parentItem.length-1])){
                            Search("notice", "");
                        }
                    }
                });
            } else {
                parentButton[i].setOnClickListener(this);
            }
        }
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.width = 300;
        lp.height = 300;

        ImageView ad = new ImageView(this);
        ad.setImageResource(R.drawable.ad);
        ad.setId(IdGen._generateViewId());
        ad.setLayoutParams(lp);
        listLayout.addView(ad);
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
            for (int i = 0; i < childitem.length; i++) {
                if (v.getId() == childButton[i].getId()) {
                    childPosition = parentPosition + i + 1;
                    isChild = true;
                    break;
                }
            }
        }

        // 손자 버튼인지 확인!
        if(!isChild && !isParent){
            // 절삭공구인지 확인
            if(cutterToolButton[0] != null) {
                for (int i = 0; i < cuttingToolItem.length; i++) {
                    if (v.getId() == cutterToolButton[i].getId()) {
                        isGrandson = true;
                        break;
                    }
                }
            }

            // 수작업 공구인지 확인
            if(handToolButton[0] != null) {
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
            // 부모 버튼 밑에 자식 버튼이 열려있지 않을 경우
            if (listLayout.getChildAt(parentPosition + 1) == parentButton[parentPosition + 1]) {
                childButton = new Button[childitem[parentPosition].length];
                for (int i = 0; i < childitem[parentPosition].length; i++) {
                    childButton[i] = new Button(this);
                    childButton[i].setId(IdGen._generateViewId());
                    childButton[i].setBackgroundColor(Color.rgb(200, 200, 200));
                    childButton[i].setText(childitem[parentPosition][i]);
                    childButton[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    listLayout.addView(childButton[i], i + parentPosition + 1);

                    // 소분류가 있는 버튼들은 다른 이벤트 리스너를 등록!
                    if (childButton[i].getText().equals("수작업공구") || childButton[i].getText().equals("절삭공구")) {
                        childButton[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (((Button) v).getText().equals("수작업공구")) {
                                    _handToolPostion = ((LinearLayout)v.getParent()).indexOfChild(v);
                                    if(!_isHandOpened) {
                                        for (int i = 0; i < handToolItem.length; i++) {
                                            handToolButton[i] = new Button(NavActivity.this);
                                            handToolButton[i].setId(IdGen._generateViewId());
                                            handToolButton[i].setBackgroundColor(Color.rgb(255, 255, 255));
                                            handToolButton[i].setText(handToolItem[i]);
                                            handToolButton[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                            listLayout.addView(handToolButton[i], _handToolPostion + i + 1);
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
                                    }
                                    else{
                                        for(int i = 0; i < handToolItem.length; i++){
                                            listLayout.removeViewAt(_handToolPostion + 1);
                                        }
                                        _isHandOpened = false;
                                    }
                                }

                                else if (((Button) v).getText().equals("절삭공구")) {
                                    _cuttingToolPosition = ((LinearLayout)v.getParent()).indexOfChild(v);
                                    if(!_isCutterOpened) {
                                        for (int i = 0; i < cuttingToolItem.length; i++) {
                                            cutterToolButton[i] = new Button(NavActivity.this);
                                            cutterToolButton[i].setId(IdGen._generateViewId());
                                            cutterToolButton[i].setBackgroundColor(Color.rgb(255, 255, 255));
                                            cutterToolButton[i].setText(cuttingToolItem[i]);
                                            cutterToolButton[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                            listLayout.addView(cutterToolButton[i], _cuttingToolPosition + i + 1);
                                            // 이벤트 리스너 등록
                                            cutterToolButton[i].setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    int pos = 11;
                                                    for (int i = 0; i < cuttingToolItem.length; i++) {
                                                        pos = i + 1;
                                                        if (((Button) v).getText().equals(cuttingToolItem[i])) {
                                                            Search(String.format("l%d", pos), "class");
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        _isCutterOpened = true;
                                    }
                                    else{
                                        for(int i = 0; i < cuttingToolItem.length; i++){
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
            }
            // 부모 버튼 밑에 자식 버튼이 추가돼 있는 경우 자식 갯수만큼 지운다.
            else {
                for (int i = parentPosition + 1; count < childitem[parentPosition].length; count++) {
                    listLayout.removeViewAt(i);
                }
            }
        }

        // 손자 없는 자식 버튼들을 클릭한 경우
        else if (isChild){
            for(int i = 0; i< childitem.length; i++){
                for(int j = 0; j < childitem[i].length; j++){
                    if(((Button)v).getText().equals(childitem[i][j])){
                        Search(childClass[i][j], "class");
                    }
                }
            }
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
                // 공지사항 띄울 시
                case "notice":
                    /*
                    c = db.query("",
                            new String[]{
                                    "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                            },
                            "isNew=1", null, null, null, null);
                    */
            }
        }
        else if (searchType.equals("search")){
                    if(searchMode.equals("제품명")){
                        c = db.query("product",
                                new String[]{
                                        "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                                },
                                "name=" + String.format("'%s'", keyword),
                                null, null, null, null);
                        Display(c);
                    }
                    else if (searchMode.equals("브랜드")){
                        c = db.query("product",
                                new String[]{
                                        "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                                },
                                "brand=" + String.format("'%s'", keyword),
                                null, null, null, null);
                        Display(c);
                    }
                    else if (searchMode.equals("규격")){
                        c = db.query("product",
                                new String[]{
                                        "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                                },
                                "standard=" + String.format("'%s'", keyword),
                                null, null, null, null);
                        Display(c);
                    }
                    else if (searchMode.equals("모델")){
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
        else if(searchType.equals("class")){
                    c = db.query("product",
                            new String[]{
                                    "pic", "name", "origin", "brand", "standard", "model", "cost", "id", "weight", "description"
                            },
                            "class=" + String.format("'%s'", keyword), null, null, null, null);
                    Display(c);
        }
    }

    private void Display(Cursor c){
        // 이미 생성돼 있는 View를 전부 삭제
        itemTable.removeAllViews();
        firstRow.removeAllViews();

        firstRow.addView(_picture);
        firstRow.addView(_productName);
        firstRow.addView(_origin);
        firstRow.addView(_brand);
        firstRow.addView(_standard);
        firstRow.addView(_model);
        firstRow.addView(_cost);

        firstRow.addView(_pickBtn);

        itemTable.addView(firstRow);

        int dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());

        TableRow.LayoutParams trParams = new TableRow.LayoutParams();
        trParams.rightMargin = dp;

        if(c.getCount() == 0){
            Toast.makeText(this, "검색결과가 없습니다!", Toast.LENGTH_SHORT).show();
        }
        else {
            while (c.moveToNext()) {
                TableRow newRow = new TableRow(this);
                newRow.setBackgroundResource(R.drawable.border);
                newRow.setGravity(Gravity.CENTER);

                // 사진, 이름, 원산지, 브랜드, 표준, 모델, 가격, 아이디
                ImageView pic = new ImageView(NavActivity.this);
                byte[] picByteData = c.getBlob(0);
                Bitmap newPicture = ByteArrayToBitmap(picByteData);
                if(newPicture != null) {
                    // 높이를 50으로 재조정한다.
                    newPicture = ResizeBitmap(newPicture, 300);
                    Drawable picDrawable = new BitmapDrawable(getResources(), newPicture);
                    pic.setImageDrawable(picDrawable);
                    pic.setId(IdGen._generateViewId());
                    pic.setOnClickListener(new PictureEvent());
                    pic.setPadding(dp, dp, 0, dp);
                    newRow.addView(pic);
                }
                else{
                    TextView noPicture = new TextView(this);
                    noPicture.setText("사진 없음");
                    noPicture.setPadding(dp, dp, 0, dp);
                    newRow.addView(noPicture);
                }

                TextView[] data = new TextView[6];

                // 이름
                data[0] = new TextView(NavActivity.this);
                data[0].setId(IdGen._generateViewId());
                data[0].setText(c.getString(1));
                data[0].setTextSize(20);
                data[0].setGravity(Gravity.CENTER_HORIZONTAL);
                newRow.addView(data[0]);

                // 원산지
                data[1] = new TextView(NavActivity.this);
                data[1].setId(IdGen._generateViewId());
                data[1].setText(c.getString(2));
                data[1].setTextSize(20);
                data[1].setGravity(Gravity.CENTER_HORIZONTAL);
                newRow.addView(data[1]);

                // 브랜드
                data[2] = new TextView(NavActivity.this);
                data[2].setId(IdGen._generateViewId());
                data[2].setText(c.getString(3));
                data[2].setTextSize(20);
                data[2].setGravity(Gravity.CENTER_HORIZONTAL);
                newRow.addView(data[2]);

                // 규격
                data[3] = new TextView(NavActivity.this);
                data[3].setId(IdGen._generateViewId());
                data[3].setText(c.getString(4));
                data[3].setTextSize(20);
                data[3].setGravity(Gravity.CENTER_HORIZONTAL);
                newRow.addView(data[3]);

                // 모델
                data[4] = new TextView(NavActivity.this);
                data[4].setId(IdGen._generateViewId());
                data[4].setText(c.getString(5));
                data[4].setTextSize(20);
                data[4].setGravity(Gravity.CENTER_HORIZONTAL);
                newRow.addView(data[4]);

                // 공급단가
                data[5] = new TextView(NavActivity.this);
                data[5].setId(IdGen._generateViewId());
                data[5].setText(c.getString(6));
                data[5].setTextSize(20);
                data[5].setGravity(Gravity.CENTER_HORIZONTAL);
                newRow.addView(data[5]);

                // 가져온 제품의 실제 DB에 등록된 아이디
                productId = c.getString(7);
                productList.add(productId);

                // ImageView와 제품 ID를 매치
                // ImageButton의 id - 실제 id
                _pictureMap.put(pic.getId(), productId);

                editBox = new EditText(this);
                editBox.setId(IdGen._generateViewId());
                editBox.setText("0");
                editBox.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(10),
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start,
                                                       int end, Spanned dest, int dstart,
                                                       int dend) {
                                Pattern ps = Pattern.compile("[0-9]");
                                if (source.equals("") || ps.matcher(source).matches()) {
                                    return source;
                                }
                                return "";
                            }
                        }
                });
                editBox.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // 지우는 경우가 아니라면
                        if (!s.toString().equals("")) {
                            // Integer 한계 초과 시
                            if(Float.parseFloat(s.toString()) >= 2147483647) {
                                Toast.makeText(NavActivity.this, "최대 갯수를 넘겼습니다!", Toast.LENGTH_SHORT).show();
                                editBox.setText(String.valueOf(2147483647));
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                quantityMap.put(/*EditBox의 id*/editBox.getId(), productId);
                editBox.setGravity(Gravity.RIGHT);
                newRow.addView(editBox);

                // 장바구니에 담는다고 표시할 체크박스 삽입
                CheckBox cb = new CheckBox(NavActivity.this);
                cb.setId(IdGen._generateViewId());
                cb.setGravity(Gravity.CENTER_HORIZONTAL);

                newRow.addView(cb);
                // 따로 배열 만들어서 어떤 놈들이 선택되었는지 저장해야 함.

                /// 추가한 Row를 TableLayout의 child로 추가해야 한다.
                itemTable.addView(newRow);
            }
            itemTable.setVisibility(View.VISIBLE);
        }
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
        if(vCategory instanceof Spinner){
            final Spinner spinner = (Spinner)vCategory;
            // 들어갈 adapter 설정
            final ArrayAdapter<CharSequence> adapter =
                    ArrayAdapter.createFromResource(this,
                            R.array.category,
                            android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // 만든 adapter 설정
            spinner.setAdapter(adapter);

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

    private class PictureEvent implements View.OnClickListener{
        @Override
        public void onClick(View v) {

            // 다이얼로그 구성 xml inflate 하기
            layoutInflator = (LayoutInflater)NavActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
            //View layout = layoutInflator.inflate(R.layout.detail_view, (ViewGroup)findViewById(R.id.detailView));

            // 상세화면에 표시할 사진 가져오기
            // 클릭한 제품의 실제 id 필요
            String pId = _pictureMap.get(v.getId());

            // 클릭한 제품의 실제 아이디를 사용하여 DB에 한 번 더 접속한다.
            // 가지고 올 내용: origin, brand, standard, weight, description
            Cursor c = DBManager.getInstance(NavActivity.this).database.query(
                    "picture",
                    new String[]{"pic"},
                    "id="+pId,
                    null, null, null, null
            );

            while(c.moveToNext()) {
                ImageView pic = new ImageView(NavActivity.this);
                byte[] picByteData = c.getBlob(0);
                Bitmap newPicture = ByteArrayToBitmap(picByteData);
                if(newPicture != null){
                    Drawable picDrawable = new BitmapDrawable(getResources(), newPicture);
                    pic.setImageDrawable(picDrawable);
                    _pictureList2.addView(pic);
                }
                else{
                    TextView noPicture = new TextView(NavActivity.this);
                    noPicture.setText("사진 없음");
                    _pictureList2.addView(noPicture);
                }
            }

            // 다이얼로그 생성 및 세팅
            AlertDialog.Builder alert = new AlertDialog.Builder(NavActivity.this);
           // alert.setView(layout);
            alert.setIcon(R.drawable.logo);
            alert.setTitle("상품 상세보기");
            alert.setNegativeButton("닫기", null);
            alert.setCancelable(true);

            // 세팅 전부 끝내고 보여주기
            alert.show();
        }
    }

    // 장바구니 이벤트 리스너
    class AddCart implements View.OnClickListener {

        TableRow r;

        int number = 0;
        int price = 0;

        String temp = "";

        String[] methodList = getResources().getStringArray(R.array.method);

        @Override
        public void onClick(View v) {
            if(itemTable.getChildCount() == 0){
                Toast.makeText(NavActivity.this,"추가할 상품이 없습니다!", Toast.LENGTH_SHORT).show();
            }
            else{
                _wholePrice = 0;
                _wholeNumber = 0;

                int count = 0;

                TableRow row = new TableRow(NavActivity.this);

                // 배송방법 선택
                Spinner method = new Spinner(NavActivity.this);
                method.setAdapter(new ArrayAdapter<>(NavActivity.this, android.R.layout.simple_spinner_dropdown_item, methodList));
                // 총 수량
                TextView productWholeNumber = new TextView(NavActivity.this);
                // 공급액 합계
                TextView productWholePrice = new TextView(NavActivity.this);

                // 선택된 상품은 수시로 바뀔 수 있으므로, 매 번 지우고 다시 확인
                productQuantity.clear();

                for(int i = 1; i < itemTable.getChildCount(); i++){
                    r = (TableRow)itemTable.getChildAt(i);
                    // 해당 상품이 선택되었다면
                    if(((CheckBox)r.getChildAt(r.getChildCount()-1)).isChecked()){
                        // 상품의 주문 개수
                        number = Integer.parseInt(((TextView)r.getChildAt(r.getChildCount()-2)).getText().toString());

                        StringTokenizer token = new StringTokenizer(((TextView)r.getChildAt(r.getChildCount()-3)).getText().toString(), ",");
                        temp = "";
                        while(token.hasMoreTokens()){
                            temp += token.nextToken();
                        }

                        // 상품 자체의 가격
                        price = Integer.parseInt(temp);

                        _wholeNumber += number;
                        _wholePrice += number*price;

                        productWholeNumber.setText(String.valueOf(_wholeNumber));
                        productWholePrice.setText(String.valueOf(_wholePrice));

                        // 선택한 상품을 실제 장바구니에 등재
                        // 실제 제품의 아이디 - 제품의 주문 수량
                        productQuantity.put(quantityMap.get((((TextView)r.getChildAt(r.getChildCount()-2)).getId())), number);

                        count++;
                    }
                }
                if(count == 0){
                    Toast.makeText(NavActivity.this, "선택된 제품이 없습니다!", Toast.LENGTH_SHORT).show();
                }
                else{
                    ((TableRow)shoppingCart.getChildAt(0)).removeAllViews();
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
                }
            }
        }
    }

    private class OrderProduct implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String orderContent = "";
            String pId = "";
            String quantity = "";

            Iterator iterProduct = productQuantity.keySet().iterator();
            TableRow r = (TableRow)((TableLayout)((TableRow)v.getParent()).getParent()).getChildAt(1);

            String shopName = "";
            String rep = "";
            String addr = "";
            String phone = "";
            String cellPhone = "";
            String freight = "";
            String fax = "";
            String selectedMethod = "";

            // 현재시간을 ms 단위로 구함
            long now = System.currentTimeMillis();

            // 일자 형식으로 변환
            Date today = new Date(now);

            // 시간 포맷 지정
            SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-dd-mm");

            // 지정된 포맷을 사용하여 String 타입으로 변환
            String todayDate = DateFormat.format(today);

            SQLiteDatabase db = DBManager.getInstance(context).database;

            // 내부 DB에서 정보 가져옴
            Cursor c = db.query("user",
                    new String[]{"shopname", "representative", "address", "phone", "cellphone", "freight", "fax"},
                    "id=" + _id,
                    null, null, null, null);

            // 받아온 데이터 저장
            while(c.moveToNext()){
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
            String method = ((Spinner)r.getChildAt(0)).getSelectedItem().toString();
            switch(method){
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

            String query = "INSERT INTO orders (shopname, representative, address, phone, cellphone, memo, sum, delivery, amount, date, id, fax, freight) ";

            /** 내부 DB 업데이트 **/
            // ordernum과 confirm은 제외한다. ordernum은 Auto-Increase 이고 confirm은 추가 승인이 필요하므로.
            db.execSQL(
                    "INSERT INTO orders (shopname, representative, address, phone, cellphone, memo, sum, delivery, amount, date, id, fax, freight) " +
                    String.format("VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');", shopName,rep, addr, phone, cellPhone, _memo.getText().toString(),
                            _wholePrice, selectedMethod, _wholeNumber, todayDate, _id, fax, freight));

            query += String.format("VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');", shopName,rep, addr, phone, cellPhone, _memo.getText().toString(),
                    _wholePrice, selectedMethod, _wholeNumber, todayDate, _id, fax, freight);

            // 가져와야 할 주문번호: 오늘(xxxx년xx월xx일 - todayDate) ~~~라고 메모를 남긴(_memo.getText().toString()) xxx가(홍길동 - _id) 한 주문
           c = db.query("orders",
                   new String[]{"ordernum"},
                   "id=? and memo=? and date=?",
                   new String[]{_id, _memo.getText().toString(), todayDate},
                   null,
                   null,
                   null
           );

            c.moveToNext();
            // 주문번호 획득
            String orderNumber = c.getString(0);

            String query2 = "insert into detailorder (name, origin, brand, standard, model, cost, amount, odernum) VALUES";

            // 가져온 주문번호를 가지고 개별적인 주문을 처리
            while(iterProduct.hasNext())
            {
                // 제품 id
                pId = iterProduct.next().toString();
                // 해당 제품의 수량
                quantity = productQuantity.get(pId).toString();

                c = db.query("product", new String[]{"name", "origin", "brand", "standard", "model", "cost"},
                        "id="+pId,
                        null,null,null,null
                );

                c.moveToNext();
                // detailorder에 개별 주문 정보를 update
                db.execSQL("insert into detailorder (name, origin, brand, standard, model, cost, amount, odernum) " +
                        String.format("VALUES ('%s','%s','%s','%s','%s','%s','%s','%s');"
                                ,c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), quantity, orderNumber));

                query2 += String.format(" ('%s','%s','%s','%s','%s','%s','%s','%s'),",
                        c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), quantity, orderNumber);
            }
            char[] t = query2.toCharArray();
            t[query2.length()-1] = ';';
            query2 = String.valueOf(t);
            /** 내부 DB 업데이트 종료 **/

            /** 서버에 업데이트 내용 전달 **/
            String updateContent = "orders="+query+"&detailorder="+query2;

            new Update().execute(updateContent);
        }
    }
}
class Update extends AsyncTask<String, String, String>{
    ProgressDialog pDialog;

    @Override
    protected void onPreExecute() {
        pDialog = ProgressDialog.show(
                /* 표시할 액티비티의 context*/ NavActivity.context,
                /* 다이얼로그 제목 문자열 */ "주문",
                /* 다이얼로그 내용 문자열 */ "진행중...",
                /* 진행여부를 알 수 있는지 유뮤. true가 알 수 없음, false가 알 수 있음 */ true,
                /* 취소 가능 여부. 기본값 false -> 이전 키 및 바깥 영역 클릭해도 취소되지 않음 */ true
                /* 다이얼로그가 취소를 알리는 리스너 -> cancelListener*/
        );
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        Order order = ((NavActivity)NavActivity.context).order;
        try {
            order.request(params[0]);
        } catch (IOException e) {
            Toast.makeText(NavActivity.context, "주문 실패! 다시 시도하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        pDialog.dismiss();
        super.onPostExecute(s);
    }
}

class Order extends NavActivity{
    // 접속대상 서버의 주소를 가진 객체
    private URL url;
    //통신을 담당하는 객체
    private HttpURLConnection conn;

    // 필요한 객체 초기화
    public Order(){
        try{
            url = new URL("http://36.39.144.65:8084/app/order.jsp");
            conn = (HttpURLConnection)url.openConnection();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void request(String parameter) throws IOException {
        String params = parameter;

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
        os.flush();
        os.write(params.getBytes("UTF-8"));

        //웹 서버에 요청하는 시점
        conn.connect();
        // 웹 서버로부터 전송받을 데이터에 대한 스트림 얻기
        InputStream is = conn.getInputStream();

        conn.disconnect();
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