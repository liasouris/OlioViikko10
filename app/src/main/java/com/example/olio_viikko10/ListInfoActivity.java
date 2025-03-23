package com.example.olio_viikko10;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ListInfoActivity extends AppCompatActivity {
    private TextView cityText;
    private TextView yearText;
    private TextView carInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_info);

        cityText = findViewById(R.id.CityText);
        yearText = findViewById(R.id.YearText);
        carInfoText = findViewById(R.id.CarInfoText);

        CarDataStorage storage = CarDataStorage.getInstance();
        cityText.setText(storage.getCity());
        yearText.setText(String.valueOf(storage.getYear()));

        ArrayList<CarData> carDataList = storage.getCarData();
        StringBuilder infoBuilder = new StringBuilder();
        for (CarData data : carDataList) {
            infoBuilder.append(data.getType())
                    .append(": ")
                    .append(data.getAmount())
                    .append("\n");
        }
        carInfoText.setText(infoBuilder.toString());
    }
}
