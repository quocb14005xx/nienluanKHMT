package nienluannganh.quocb14005xx.nienluannganhkhmt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Spinner spFrom,spTo;//spiner
    private ImageButton btnSwitch,btnHearingInput,btnRecord,btnHearingOutput,btnCopy;
    private Button btnTranslate;
    private EditText edtInput;
    private TextView txtOutput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init()
    {
        //anh xa view
        spFrom=findViewById(R.id.spinerFrom);
        spTo=findViewById(R.id.spinerTo);
        btnSwitch=findViewById(R.id.btnSwitch);
        btnHearingInput=findViewById(R.id.btnHearingInput);
        btnRecord=findViewById(R.id.btnRecord);
        btnHearingOutput=findViewById(R.id.btnHearingOutput);
        btnCopy=findViewById(R.id.btnCopy);
        btnTranslate=findViewById(R.id.btnTranslate);
        edtInput=findViewById(R.id.edtInput);
        txtOutput=findViewById(R.id.txtOutput);



        //khởi tạo giá trị cần thiết
        ArrayList<String> listLanguage = new ArrayList<>();
        listLanguage.add("Việt Nam");
        listLanguage.add("English");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,listLanguage);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spFrom.setAdapter(adapter);
        spTo.setAdapter(adapter);


    }

}
