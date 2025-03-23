package com.example.olio_viikko10;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private EditText cityNameEdit;
    private EditText yearEdit;
    private Button searchButton;
    private Button listInfoActivityButton;
    private TextView statusText;
    private Button returnHomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        cityNameEdit = findViewById(R.id.CityNameEdit);
        yearEdit = findViewById(R.id.YearEdit);
        searchButton = findViewById(R.id.SearchButton);
        listInfoActivityButton = findViewById(R.id.ListInfoActivityButton);
        statusText = findViewById(R.id.StatusText);
        returnHomeButton = findViewById(R.id.ReturnHomeButton);

        returnHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
        });

        listInfoActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, ListInfoActivity.class);
            startActivity(intent);
        });

        searchButton.setOnClickListener(v -> {
            String city = cityNameEdit.getText().toString().trim();
            String yearStr = yearEdit.getText().toString().trim();

            if (TextUtils.isEmpty(city) || TextUtils.isEmpty(yearStr)) {
                statusText.setText("Haku epäonnistui: kentät tyhjiä");
                return;
            }

            int year;
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                statusText.setText("Haku epäonnistui: täytyy olla numero");
                return;
            }

            statusText.setText("Haetaan");
            new Thread(() -> {
                boolean success = getData(SearchActivity.this, city, year);
                runOnUiThread(() -> {
                    if(success) {
                        statusText.setText("Haku onnistui");
                    } else {
                        statusText.setText("Haku epäonnistui: virhe haussa");
                    }
                });
            }).start();
        });

    }

    public boolean getData(Context context, String city, int year) {
        CarDataStorage storage = CarDataStorage.getInstance();
        storage.setCity(city);
        storage.setYear(year);
        storage.clearData();

        try {
            ObjectMapper mapper = new ObjectMapper();

            URL getUrl = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px");
            HttpURLConnection getCon = (HttpURLConnection) getUrl.openConnection();
            getCon.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(getCon.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            JsonNode filtersNode = mapper.readTree(sb.toString());

            JsonNode areaVar = filtersNode.get("variables").get(0);
            ArrayList<String> areaCodes = new ArrayList<>();
            ArrayList<String> areaNames = new ArrayList<>();
            for (JsonNode node : areaVar.get("values")) {
                areaCodes.add(node.asText());
            }
            for (JsonNode node : areaVar.get("valueTexts")) {
                areaNames.add(node.asText());
            }
            HashMap<String, String> areaMap = new HashMap<>();
            for (int i = 0; i < areaNames.size(); i++) {
                areaMap.put(areaNames.get(i).toLowerCase(), areaCodes.get(i));
            }
            String areaCode = areaMap.get(city.toLowerCase());
            if(areaCode == null) {
                return false;
            }

            InputStream is = context.getResources().openRawResource(R.raw.query);
            JsonNode queryTemplate = mapper.readTree(is);

            ((ObjectNode) queryTemplate.get("query").get(0).get("selection")).removeAll();
            ((ObjectNode) queryTemplate.get("query").get(0).get("selection")).put("filter", "item");

            ArrayNode areaArray = mapper.createArrayNode();
            areaArray.add(areaCode);

            ((ObjectNode) queryTemplate.get("query").get(0).get("selection")).set("values", areaArray);
            ((ObjectNode) queryTemplate.get("query").get(3).get("selection")).removeAll();
            ((ObjectNode) queryTemplate.get("query").get(3).get("selection")).put("filter", "item");

            ArrayNode yearArray = mapper.createArrayNode();
            yearArray.add(String.valueOf(year));

            ((ObjectNode) queryTemplate.get("query").get(3).get("selection")).set("values", yearArray);

            URL postUrl = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px");
            HttpURLConnection postCon = (HttpURLConnection) postUrl.openConnection();
            postCon.setRequestMethod("POST");
            postCon.setRequestProperty("Content-Type", "application/json; utf-8");
            postCon.setRequestProperty("Accept", "application/json");
            postCon.setDoOutput(true);

            byte[] jsonInput = mapper.writeValueAsBytes(queryTemplate);
            OutputStream os = postCon.getOutputStream();
            os.write(jsonInput, 0, jsonInput.length);
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(postCon.getInputStream(), "utf-8"));
            StringBuilder postResponse = new StringBuilder();
            while ((line = br.readLine()) != null) {
                postResponse.append(line.trim());
            }
            br.close();

            JsonNode responseData = mapper.readTree(postResponse.toString());

            ArrayNode valuesNode = (ArrayNode) responseData.get("value");
            if (valuesNode == null || valuesNode.size() < 5) {
                return false;
            }
            String[] types = {"Henkilöautot", "Pakettiautot", "Kuorma-autot", "Linja-autot", "Erikoisautot"};
            int total = 0;
            for (int i = 0; i < types.length; i++) {
                int amount = valuesNode.get(i).asInt();
                total += amount;
                storage.addCarData(new CarData(types[i], amount));
            }
            storage.addCarData(new CarData("Yhteensä", total));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}