package nienluannganh.quocb14005xx.nienluannganhkhmt.activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.detectlanguage.errors.APIError;

import java.util.ArrayList;

import java.util.Locale;

import nienluannganh.quocb14005xx.nienluannganhkhmt.R;
import nienluannganh.quocb14005xx.nienluannganhkhmt.utils.MyConstants;
import nienluannganh.quocb14005xx.nienluannganhkhmt.utils.Translator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static int SPIN1_SPIN2 = 1;


    private Spinner spFrom, spTo;//spiner
    private ImageButton btnSwitch, btnHearingInput, btnRecord, btnHearingOutput, btnCopy;
    private Button btnTranslate;
    private EditText edtInput;
    private TextView txtOutput;
    private ArrayList<String> listLanguage1;
    private ArrayList<String> listLanguage2;
    private TextToSpeech tts;
    private Translator translator;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        init();
        events();
    }

    private void events() {
        /*
         *
         * event click
         *
         * */
        btnSwitch.setOnClickListener(this);
        btnRecord.setOnClickListener(this);
        btnHearingInput.setOnClickListener(this);
        btnTranslate.setOnClickListener(this);
        btnHearingOutput.setOnClickListener(this);
        btnCopy.setOnClickListener(this);
        /*
         *
         * event spiner
         * spFrom 2 spTo  || spTo 2 spFrom
         *
         * */


        spFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                spTo.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spFrom.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void init() {
        //anh xa view
        spFrom = findViewById(R.id.spinerFrom);
        spTo = findViewById(R.id.spinerTo);
        btnSwitch = findViewById(R.id.btnSwitch);
        btnHearingInput = findViewById(R.id.btnHearingInput);
        btnRecord = findViewById(R.id.btnRecord);
        btnHearingOutput = findViewById(R.id.btnHearingOutput);
        btnCopy = findViewById(R.id.btnCopy);
        btnTranslate = findViewById(R.id.btnTranslate);
        edtInput = findViewById(R.id.edtInput);
        txtOutput = findViewById(R.id.txtOutput);

        //TTS
        tts = new TextToSpeech(this, this);
        tts.setLanguage(new Locale("vi", "VN"));

        //khởi tạo giá trị cần thiết
        listLanguage1 = new ArrayList<>();
        listLanguage1.add("Việt Nam");
        listLanguage1.add("English");
        listLanguage2 = new ArrayList<>();
        listLanguage2.add("English");
        listLanguage2.add("Việt Nam");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, listLanguage1);
        adapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, listLanguage2);
        adapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spFrom.setAdapter(adapter1);
        spTo.setAdapter(adapter2);
//        spTo.setSelection(listLanguage.size() - 1);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSwitch:
                //lấy text của mỗi spin
                String textFrom = spFrom.getSelectedItem().toString();
                String textTo = spTo.getSelectedItem().toString();

                //tìm text của spin 1 là index mấy bên spin2 và ngược lại
                int indexFrom = listLanguage1.indexOf(textTo);
                int indexTo = listLanguage2.indexOf(textFrom);

                //setIndex
                spFrom.setSelection(indexFrom);
                spTo.setSelection(indexTo);

                break;
            case R.id.btnRecord:
                promptSpeechInput();


                break;
            case R.id.btnHearingInput:
                if (getStatusFromVaTo() == 1) {
                    tts.setLanguage(new Locale("vi"));
                } else {
                    tts.setLanguage(Locale.US);
                }
                speakOut(edtInput.getText().toString());
                break;
            case R.id.btnHearingOutput:
                if (getStatusFromVaTo() == 1) {
                    tts.setLanguage(new Locale("vi"));
                } else {
                    tts.setLanguage(Locale.US);
                }
                String temp[] = txtOutput.getText().toString().split("===>");
                if (temp[1]!=null)
                {
                    Toast.makeText(this, temp[1], Toast.LENGTH_SHORT).show();
                    speakOut(temp[1]);
                }
                else
                {
                    Snackbar.make(findViewById(R.id.ln),"Output rỗng ko đọc!",Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnTranslate:
                if (edtInput.getText().toString().length()<1)
                {
                    Snackbar.make(findViewById(R.id.ln),"Nhập chữ vào input để dịch!",Snackbar.LENGTH_SHORT).show();
                }
                else
                {
                    if (getTrangThaiWifi())
                    {
                        new TranslatorTask().execute();
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.ln),"Vui lòng Kiểm tra wifi mở chưa ?",Snackbar.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.btnCopy:

                break;
        }
    }



    //show dialog voice và intent đến action của reconizer
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

    /**
     * onActivityResult
     * callback trả về sau sau khi intent gọi speech regconizer của hệ thống android
     * lấy kết quả và xử lý lại đoạn text thông qua class Translator
     */
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
                        StringBuilder tbt = translator.getDetectedNgonNgu(result.get(0),getStatusFromVaTo());
                        edtInput.setText(tbt.toString());
                    } catch (APIError apiError) {
                        apiError.printStackTrace();
                    }
                }
                break;
            }
        }
    }


    /**
     * 3 hàm onInit,speakOut , vòng đời onDestroy dành cho text to speech
     * <p>
     * onInit : khởi tạo speech to text cho quá trình phát âm thanh
     * speakOut : nhận text input và phát âm thanh sau khi onInit thành công
     * onDestroy : activity sẽ bị destroy và sẽ hủy lươn tiến trình của text to speech
     */
    //init text to speech
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(new Locale("vi"));

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(MyConstants.LOG, "This Language is not supported");
            }
        } else {
            Log.e(MyConstants.LOG, "Initilization Failed!");
        }
    }


    //speak text from input
    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }


    // luồng dịch
    class TranslatorTask extends AsyncTask<Void, Void, Void> {

        //start luồng
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, null, "Đợi đang dịch....");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            super.onPreExecute();
        }


        //tiến trình xử lý luồng
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        //xử lý luông cho translate
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                translator = new Translator(MyConstants.CLOUD_API_KEY);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        //kết thúc luồng
        @Override
        protected void onPostExecute(Void aVoid) {
            progress.dismiss();
            super.onPostExecute(aVoid);
            translated();
        }
    }

    /*
     * SPIN1_SPIN2 lằ hằng số để đánh dấu trạng thái đang là ngôn ngữ nào sang ngôn ngữ nào
     * 1 là vn-en
     * 2 là en-vn
     * */
    private int getStatusFromVaTo() {
        if (spFrom.getSelectedItem().toString().equals("Việt Nam") && spTo.getSelectedItem().toString().equals("English")) {
            SPIN1_SPIN2 = 1;//
        } else if (spFrom.getSelectedItem().toString().equals("English") && spTo.getSelectedItem().toString().equals("Việt Nam")) {
            SPIN1_SPIN2 = 2;
        }
        return SPIN1_SPIN2;
    }

    public void translated() {
        String text = null;
        switch (getStatusFromVaTo()) {
            case 1:
                text = translator.Translating(edtInput.getText().toString(), "vi", "en");
                break;
            case 2:
                text = translator.Translating(edtInput.getText().toString(), "en", "vi");
                break;

        }
        txtOutput.setText(edtInput.getText().toString() + "===>" + text);
    }
    private boolean getTrangThaiWifi()
    {
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }
}
