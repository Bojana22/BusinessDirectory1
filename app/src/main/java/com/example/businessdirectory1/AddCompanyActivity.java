package com.example.businessdirectory1;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddCompanyActivity extends AppCompatActivity {

    private EditText etName, etAddress, etLat, etLon, etEmail, etPhone, etWeb;
    private CheckBox cbIndustry, cbFun, cbEducation, cbServices;
    private Spinner iconSpinner;

    private String[] iconNames = {"Factory", "University", "Coffee Shop", "Cinema", "Auto Repair", "Tech/Computer"};

    private int[] iconResources = {
            R.drawable.factory,
            R.drawable.university,
            R.drawable.coffee,
            R.drawable.cinema,
            R.drawable.car,
            R.drawable.technology
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company);

        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Company");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // initialize views
        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etLat = findViewById(R.id.etLatitude);
        etLon = findViewById(R.id.etLongitude);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etWeb = findViewById(R.id.etWebsite);
        cbIndustry = findViewById(R.id.cbIndustry);
        cbFun = findViewById(R.id.cbFun);
        cbEducation = findViewById(R.id.cbEducation);
        cbServices = findViewById(R.id.cbServices);
        iconSpinner = findViewById(R.id.iconSpinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, iconNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        iconSpinner.setAdapter(spinnerAdapter);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveCompany());
    }

    private void saveCompany() {
        String name = etName.getText().toString();
        String address = etAddress.getText().toString();
        String latStr = etLat.getText().toString();
        String lonStr = etLon.getText().toString();
        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();
        String web = etWeb.getText().toString();

        // Конверзија на локацијата
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            if (!latStr.isEmpty()) latitude = Double.parseDouble(latStr);
            if (!lonStr.isEmpty()) longitude = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Внесете валидни координати!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder categoryBuilder = new StringBuilder();

        if (cbServices.isChecked()) {
            categoryBuilder.append("Services,");
        }

        if (cbFun.isChecked()) {
            categoryBuilder.append("Fun,");
        }

        if (cbIndustry.isChecked()) {
            categoryBuilder.append("Industry,");
        }

        if (cbEducation.isChecked()) {
            categoryBuilder.append("Education,");
        }

        String category = categoryBuilder.toString();

        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Пополнете име и категорија!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = iconSpinner.getSelectedItemPosition();
        int imageRes = iconResources[selectedPosition];

        // Креирање објект
        Company newCompany = new Company(name, address, latitude, longitude, email, phone, web, category, imageRes);

        // Снимање во база (локална Room база)
        new Thread(() -> {
            AppDatabase.getInstance(AddCompanyActivity.this).companyDao().insert(newCompany);
            runOnUiThread(() -> {
                Toast.makeText(AddCompanyActivity.this, "Зачувано успешно!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}