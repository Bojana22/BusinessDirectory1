package com.example.businessdirectory1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // 1. Преземање на објектот пратен преку Intent
        // Користиме "company_data" како клуч, исто како во адаптерот
        Company company = (Company) getIntent().getSerializableExtra("company_data");

        // 2. Поврзување со XML елементите
        ImageView logoImg = findViewById(R.id.detailsLogo);
        TextView nameTxt = findViewById(R.id.detailsName);
        TextView addressTxt = findViewById(R.id.detailsAddress);
        TextView phoneTxt = findViewById(R.id.detailsPhone);
        TextView webTxt = findViewById(R.id.detailsWeb);
        TextView emailTxt = findViewById(R.id.detailsEmail); // Провери дали го имаш ова во XML
        TextView latTxt = findViewById(R.id.detailsLatitude);
        TextView lonTxt = findViewById(R.id.detailsLongitude);
        Button btnMap = findViewById(R.id.btnOpenMap);

        if (company != null) {
            // 3. Поставување на податоците
            nameTxt.setText(company.getName());
            addressTxt.setText(company.getAddress());
            phoneTxt.setText("Тел: " + company.getPhone());
            webTxt.setText("Web: " + company.getWebsite());

            // Додадено за емаил (бидејќи го имаш во моделот)
            if (emailTxt != null) {
                emailTxt.setText("Email: " + company.getEmail());
            }

            // ПОПРАВЕНО: Користи го точното име на методот од твојата Company класа
            logoImg.setImageResource(company.getIconResId());

            // 4. Координати (double)
            double lat = company.getLatitude();
            double lon = company.getLongitude();

            latTxt.setText("Latitude: " + lat);
            lonTxt.setText("Longitude: " + lon);

            // 5. Копче за Мапа
            btnMap.setOnClickListener(v -> {
                // Формат: geo:lat,lon?q=lat,lon(Име)
                String uri = "geo:" + lat + "," + lon + "?q=" + lat + "," + lon + "(" + Uri.encode(company.getName()) + ")";
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

                // Проверка дали уредот има апликација за мапи (Google Maps)
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Ако нема Google Maps, отвори во прелистувач
                    String webUri = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUri)));
                }
            });

            Toast.makeText(this, "Приказ на: " + company.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Грешка: Не се пронајдени податоци!", Toast.LENGTH_LONG).show();
            finish(); // Затвори ја активноста ако нема податоци
        }
    }
}