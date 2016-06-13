package kr.dy.yu.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btn_Register;
    Button btn_Ok;
    Button btn_Register_back;
    Button btn_Register_reset;
    Button btn_Register_ok;

    public LoadManager loadMgr;

    // 로그인 아이디, 비밀번호
    EditText idForm;
    EditText pwForm;

    String id = "";
    String pw = "";

    // 아이디, 비밀번호, 상호명, 대표자명, 주소, 휴대전화번호, 전화번호, 사업자번호, 화물지점, 팩스번호
    EditText idRegisterForm;
    EditText pwRegisterForm;
    EditText shopNameForm;
    EditText representativeForm;
    EditText addressForm;
    EditText cellPhoneForm;
    EditText phoneForm;
    EditText licenseeForm;
    EditText freightForm;
    EditText faxForm;

    LinearLayout layout_login;
    ScrollView layout_register;

    private String _idToRegister = "";
    private String _pwToRegister = "";
    private String _shopName = "";
    private String _executive = "";
    private String _address = "";
    private String _cellPhone = "";
    private String _phone = "";
    private String _licenseeNumber = "";
    private String _freightBranch = "";
    private String _faxNumber = "";

    MyAsyncTask task;


    //인터넷에서 데이터를 가져오기 전에 연결상태부터 확인!
    public void connectStatusCheck(String action){
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = connMgr.getActiveNetworkInfo();
        // 아이디 누락 시
        if(idForm.getText().toString().equals("") && action == "login"){
            Toast.makeText(this, "아이디를 입력해주세요!", Toast.LENGTH_SHORT).show();
        }
        // 비밀번호 누락 시
        else if(pwForm.getText().toString().equals("") && action == "login"){
            Toast.makeText(this, "비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show();
        }
        // 필수 정보 누락 시
        else if((this._idToRegister.equals("") || this._pwToRegister.equals("") || this._shopName.equals("")
                || this._executive.equals("") || this._address.equals("") || this._cellPhone.equals("")
                || this._licenseeNumber.equals("") || this._freightBranch.equals("")) && action.equals("register")){
            Toast.makeText(this, "필수정보가 누락되었습니다!", Toast.LENGTH_SHORT).show();
        }

        else if(networkinfo != null && networkinfo.isConnected()){
            task = new MyAsyncTask(this);
            loadMgr = new LoadManager();
            // 네트워크 연결
            if(action.equals("login")){
                task.SetAction("login");
                task.execute(id, pw);
            }
            else if (action.equals("register")){
                task.SetAction("register");
                task.execute(_idToRegister, _pwToRegister, _shopName, _executive,
                        _address, _cellPhone, _phone, _licenseeNumber, _freightBranch, _faxNumber);
            }
        }
        else{
            // 네트워크 미연결
            Toast.makeText(this, "네트워크가 연결되어 있지 않습니다. 연결상태를 확인해 주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("로그인");

        DBManager.getInstance(this);

        btn_Ok = (Button)findViewById(R.id.btn_login);
        btn_Register = (Button)findViewById(R.id.btn_reg);
        btn_Register_back = (Button)findViewById(R.id.btn_back);
        btn_Register_reset = (Button)findViewById(R.id.btn_reset);
        btn_Register_ok = (Button)findViewById(R.id.btn_reg_ok);

        // 로그인 폼 초기화
        idForm = (EditText)findViewById(R.id.idForm);
        pwForm = (EditText)findViewById(R.id.pwForm);

        // 등록 폼 초기화
        idRegisterForm = (EditText)findViewById(R.id.idRegisterForm);
        pwRegisterForm = (EditText)findViewById(R.id.pwRegisterForm);
        shopNameForm = (EditText)findViewById(R.id.shopName);
        representativeForm = (EditText)findViewById(R.id.executiveName);
        addressForm = (EditText)findViewById(R.id.address);
        cellPhoneForm = (EditText)findViewById(R.id.cellPhone);
        phoneForm = (EditText)findViewById(R.id.phoneNumber);
        licenseeForm = (EditText)findViewById(R.id.licenseeNumber);
        freightForm = (EditText)findViewById(R.id.freightBranch);
        faxForm = (EditText)findViewById(R.id.faxNumber);

        idForm.setText("");
        pwForm.setText("");

        idRegisterForm.setText("");
        pwRegisterForm.setText("");
        shopNameForm.setText("");
        representativeForm.setText("");
        addressForm.setText("");
        cellPhoneForm.setText("");
        phoneForm.setText("");
        licenseeForm.setText("");
        freightForm.setText("");
        faxForm.setText("");

        // 아이디 등록 칸에 사용 한글 사용불가능
        idRegisterForm.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(20),
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart,
                                               int dend)
                    {
                        Pattern ps = Pattern.compile("^[-_a-zA-Z0-9]+$");
                        if(source.equals("") || ps.matcher(source).matches()) {
                            return source;
                        }
                        return "";
                    }
                }
        });

        // 아이디 로그인 칸에 사용 한글 사용불가능
        idForm.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(20),
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart,
                                               int dend)
                    {
                        Pattern ps = Pattern.compile("^[-_a-zA-Z0-9]+$");
                        if(source.equals("") || ps.matcher(source).matches()) {
                            return source;
                        }
                        return "";
                    }
                }
        });

        //각 레이아웃들
        layout_login = (LinearLayout)findViewById(R.id.layer_login);
        layout_register = (ScrollView)findViewById(R.id.layout_reg);

        layout_login.setVisibility(View.VISIBLE);
        layout_register.setVisibility(View.INVISIBLE);

        // 버튼들을 클릭 이벤트 리스너에 등록
        btn_Ok.setOnClickListener(this);
        btn_Register.setOnClickListener(this);
        btn_Register_back.setOnClickListener(this);
        btn_Register_reset.setOnClickListener(this);
        btn_Register_ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == btn_Ok){
            this.id = idForm.getText().toString();
            this.pw = pwForm.getText().toString();
            connectStatusCheck("login");
        }
        else if(v == btn_Register){
            layout_login.setVisibility(View.INVISIBLE);
            layout_register.setVisibility(View.VISIBLE);
            this.setTitle("회원 등록");
        }
        else if(v == btn_Register_back){
            layout_register.setVisibility(View.INVISIBLE);
            layout_login.setVisibility(View.VISIBLE);
        }
        else if(v == btn_Register_ok){
            _idToRegister = idRegisterForm.getText().toString();
            _pwToRegister = pwRegisterForm.getText().toString();
            _shopName = shopNameForm.getText().toString();
            _executive = representativeForm.getText().toString();
            _address = addressForm.getText().toString();
            _cellPhone = cellPhoneForm.getText().toString();
            _phone = phoneForm.getText().toString();
            _licenseeNumber = licenseeForm.getText().toString();
            _freightBranch = freightForm.getText().toString();
            _faxNumber = faxForm.getText().toString();
            connectStatusCheck("register");
        }
        else if(v == btn_Register_reset){
            idRegisterForm.setText("");
            pwRegisterForm.setText("");
            shopNameForm.setText("");
            representativeForm.setText("");
            addressForm.setText("");
            cellPhoneForm.setText("");
            phoneForm.setText("");
            licenseeForm.setText("");
            freightForm.setText("");
            faxForm.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        if(layout_register.getVisibility() == View.VISIBLE){
            setTitle("로그인");
            layout_register.setVisibility(View.INVISIBLE);
            layout_login.setVisibility(View.VISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }
}
