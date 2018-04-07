package nienluannganh.quocb14005xx.nienluannganhkhmt.utils;

import com.detectlanguage.DetectLanguage;
import com.detectlanguage.Result;
import com.detectlanguage.errors.APIError;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by quocb14005xx on 3/22/2018.
 */

public class Translator {
    private String key;

    public Translator() {
    }

    public Translator(String key) {
        this.key = key;
    }

    public String Translating(String text, String from, String to) {
        StringBuilder result = new StringBuilder();
        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String urlStr = "https://www.googleapis.com/language/translate/v2?key=" + key + "&q=" + encodedText + "&target=" + to + "&source=" + from;

            URL url = new URL(urlStr);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            InputStream stream;
            if (conn.getResponseCode() == 200) //success
            {
                stream = conn.getInputStream();
            } else
                stream = conn.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            JsonParser parser = new JsonParser();

            JsonElement element = parser.parse(result.toString());
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.get("error") == null) {
                    String translatedText = obj.get("data").getAsJsonObject().
                            get("translations").getAsJsonArray().
                            get(0).getAsJsonObject().
                            get("translatedText").getAsString();
                    return translatedText;

                }
            }
            if (conn.getResponseCode() != 200) {
                System.err.println(result);
            }

        } catch (IOException | JsonSyntaxException ex) {

            System.err.println(ex.getMessage());

        }

        return null;
    }


    /*
    * lấy đoạn text nhận được sau khi regconize của hệ thống do không xác định được việt hay english cho nên sẽ qua xử lý trung giang thông qua
    * method getDêtctedNgonNgu
    * @đối số 1 : input text lẫn lộn tiếng việt tiếng anh
    * @đói số 2: ngôn ngữ đọc cần được lọc
    *
    * @return StringBuilder appen các từ thỏa điều kiện
    *
    * */
    public StringBuilder getDetectedNgonNgu(String input, int ngon_ngu) throws APIError {
        DetectLanguage.apiKey = "dccdc9da93ca50eae67158d0b1f8b957";
        String[] temp = input.split(" ");
        StringBuilder tb = new StringBuilder();
        switch (ngon_ngu) {
            case 1:
                for (int i = 0; i < temp.length; i++) {
                    List<Result> results = DetectLanguage.detect(temp[i]);
                    Result result = results.get(0);
                    if (result.language.equals("vi")) {
                        tb.append(temp[i] + " ");
                    }
                }
                break;
            case 2:
                for (int i = 0; i < temp.length; i++) {
                    List<Result> results = DetectLanguage.detect(temp[i]);
                    Result result = results.get(0);
                    if (result.language.equals("en")) {
                        tb.append(temp[i] + " ");
                    }
                }
                break;
        }



        return tb;
    }

}
