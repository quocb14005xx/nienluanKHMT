package nienluannganh.quocb14005xx.nienluannganhkhmt.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nienluannganh.quocb14005xx.nienluannganhkhmt.R;
import nienluannganh.quocb14005xx.nienluannganhkhmt.utils.MyConstants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    String base64EncodedData;
    private Spinner spFrom, spTo;//spiner
    private ImageButton btnSwitch, btnHearingInput, btnRecord, btnHearingOutput, btnCopy;
    private Button btnTranslate;
    private EditText edtInput;
    private TextView txtOutput;
    private ArrayList<String> listLanguage;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        events();
    }

    private void events() {
        btnSwitch.setOnClickListener(this);
        btnRecord.setOnClickListener(this);
        btnHearingInput.setOnClickListener(this);
        btnTranslate.setOnClickListener(this);
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
        listLanguage = new ArrayList<>();
        listLanguage.add("Việt Nam");
        listLanguage.add("English");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, listLanguage);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spFrom.setAdapter(adapter);
        spTo.setAdapter(adapter);
        spTo.setSelection(listLanguage.size() - 1);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSwitch:
                String textFrom = spFrom.getSelectedItem().toString();
                String textTo = spTo.getSelectedItem().toString();

                int indexFrom = listLanguage.indexOf(textFrom);
                int indexTo = listLanguage.indexOf(textTo);

                spFrom.setSelection(indexTo);
                spTo.setSelection(indexFrom);

                break;
            case R.id.btnRecord:
                promptSpeechInput();

//                try {
//                    syncRecognizeFile(MyConstants.LINK_TEST_LOCAL_FILE);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                break;
            case R.id.btnHearingInput:
                speakOut();
                break;
            case R.id.btnTranslate:
                Toast.makeText(this, edtInput.getText().toString(), Toast.LENGTH_SHORT).show();
                break;
        }
    }


    public static void syncRecognizeFile(String fileName) throws Exception, IOException {
        //xac thuc tai đây
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        SpeechClient speech = SpeechClient.create();

        @SuppressLint({"NewApi", "LocalSuppress"}) Path path = Paths.get(fileName);
        @SuppressLint({"NewApi", "LocalSuppress"}) byte[] data = Files.readAllBytes(path);
        ByteString audioBytes = ByteString.copyFrom(data);

        // Configure request with local raw PCM audio
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setLanguageCode("en-US")
                .setSampleRateHertz(16000)
                .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(audioBytes)
                .build();
        // Use blocking call to get audio transcript
        RecognizeResponse response = speech.recognize(config, audio);
        List<SpeechRecognitionResult> results = response.getResultsList();

        for (SpeechRecognitionResult result: results) {
            // There can be several alternative transcripts for a given chunk of speech. Just use the
            // first (most likely) one here.
            SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
            System.out.printf("Transcription: %s%n", alternative.getTranscript());
        }
        speech.close();
    }


    //show dialog voice và intent đến action của reconizer
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
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
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    edtInput.setText(result.get(0));
                }
                break;
            }
        }
    }


    /**
     * 3 hàm onInit,speakOut , vòng đời onDestroy dành cho text to speech
     *
     * onInit : khởi tạo speech to text cho quá trình phát âm thanh
     * speakOut : nhận text input và phát âm thanh sau khi onInit thành công
     * onDestroy : activity sẽ bị destroy và sẽ hủy lươn tiến trình của text to speech
     * */
    //init text to speech
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(MyConstants.LOG, "This Language is not supported");
            } else {
                speakOut();
            }

        } else {
            Log.e(MyConstants.LOG, "Initilization Failed!");
        }
    }


    //speak text from input
    private void speakOut() {
        String text = edtInput.getText().toString();
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
}
