package nienluannganh.quocb14005xx.nienluannganhkhmt.activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.detectlanguage.errors.APIError;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import nienluannganh.quocb14005xx.nienluannganhkhmt.R;
import nienluannganh.quocb14005xx.nienluannganhkhmt.utils.MyConstants;
import nienluannganh.quocb14005xx.nienluannganhkhmt.utils.Translator;

public class Main2Activity extends AppCompatActivity {
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private Button btnCham;
    private TextView txtShowDiem,txtC1,txtC2,txtCCorrect;
    private ProgressBar progressBarShowDiem;
    private EditText edtInput2;
    private Translator translator;
    private static int ngonngu=1;
    private int diem;
    private StringBuilder tbt_NhanDangXong;
    private String tbt_NgheDuoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportActionBar().show();
        init();
        events();
    }

    private void events()
    {
        btnCham.setOnClickListener(onLick);
    }
    private static int i=0;
    private void refreshInstance()
    {
        diem=0;
        edtInput2.setText("");
        txtShowDiem.setText("Bạn được "+ diem + "/10 tổng số điểm! ");
        txtC2.setText("");
        txtC1.setText("");
        txtCCorrect.setText("");
        progressBarShowDiem.setProgress(0);
    }
    View.OnClickListener onLick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            i=0;

            if (!tbt_NhanDangXong.equals("") && !edtInput2.getText().toString().equals("") )//điều kiện phải nhật,phải nói
                {
                    if (!tbt_NgheDuoc.equals(""))
                    {
                        String []strNhanDangXong=tbt_NhanDangXong.toString().split(" ");
                        Log.e(MyConstants.LOG, "onClick: strNhandangxong " + String.valueOf(strNhanDangXong.length));
                        String []abcT=edtInput2.getText().toString().split(" ");
                        Log.e(MyConstants.LOG, "onClick: length input " + String.valueOf(abcT.length));

                        StringBuilder tbt_NhanDangXongNew = new StringBuilder();

                        for (String arg : strNhanDangXong) {
                            for (String arg2 : abcT)
                            {
                                if(arg.equalsIgnoreCase(arg2))
                                {
                                    tbt_NhanDangXongNew.append(arg2+ " ");
                                }
                            }
                        }
                        Log.e(MyConstants.LOG, "onClick: Cham diem TBT  " + tbt_NhanDangXongNew.toString() );
                        diem=(int)(chamDiem(tbt_NhanDangXongNew,edtInput2.getText().toString()));
                        if (diem!=0)
                        {
                            final Timer timer = new Timer();
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (i==diem)
                                            {
                                                timer.cancel();
                                            }
                                            progressBarShowDiem.setProgress(i);
                                            i++;
                                        }
                                    });
                                }
                            };
                            timer.schedule(timerTask,0,200);
                        }
                        txtShowDiem.setText("Bạn được "+ diem + "/10 tổng số điểm! ");
                        txtC2.setText(edtInput2.getText().toString()+"");
                        txtC1.setText(tbt_NgheDuoc.toString());
                        txtCCorrect.setText(tbt_NhanDangXongNew.toString());
                    }
                   else
                    {
                        Toast.makeText(Main2Activity.this, "Nói ngôn ngữ bạn cần test!", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.ln2),"Đọc giộng nói và nhập đề bài của bạn trước!",Snackbar.LENGTH_SHORT).show();
                }

        }
    };
    /*
    * @speechChuaNhanDan so sánh với speechDaNhanDang xem nói đúng bao nhiu từ theo tiêu chí tiếng việt hay viết anh
    * */
    private float chamDiem(StringBuilder speechDaNhanDang,String debai)
    {
        String speechDaNhanDangStr=speechDaNhanDang.toString();
        float numWord1=speechDaNhanDangStr.split(" ").length;
        float numWord2=debai.split(" ").length;
        float t_diem=numWord1/numWord2*10;
        return t_diem;
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói gì đó đi");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn\\'t support speech input",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    translator = new Translator(MyConstants.CLOUD_API_KEY);
                    try {
                        tbt_NhanDangXong = translator.getDetectedNgonNgu(result.get(0),ngonngu);
                        Log.e(MyConstants.LOG, "onActivityResult: " + tbt_NhanDangXong );
                        tbt_NgheDuoc=result.get(0);
                        txtC1.setText(tbt_NgheDuoc);
                    } catch (APIError apiError) {
                        apiError.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    private void init()
    {
        tbt_NhanDangXong = new StringBuilder();
        tbt_NgheDuoc="";
        btnCham=findViewById(R.id.btnChamDiem);
        txtShowDiem=findViewById(R.id.txtShowDiem);
        txtCCorrect=findViewById(R.id.txtContentCorrect);
        txtC1=findViewById(R.id.txtContent1);
        txtC2=findViewById(R.id.txtContent2);
        progressBarShowDiem=findViewById(R.id.progressShowDiem);
        edtInput2=findViewById(R.id.edtInput2);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_ngonngu_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_vn:
                ngonngu=1;
                promptSpeechInput();
                return true;
            case R.id.action_eng:
                ngonngu=2;
                promptSpeechInput();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
