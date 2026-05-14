package com.example.businessdirectory1;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private DrawerLayout drawer;
    private AppDatabase db;
    private ViewPagerAdapter adapter;

    private final List<Fragment> fragments = new ArrayList<>();
    private final List<Company> allCompanies = new ArrayList<>();

    private static final String CHANNEL_ID = "nearby_company_channel";
    private static final float PROXIMITY_RADIUS = 50f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        createNotificationChannel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawer,
                        toolbar,
                        R.string.open,
                        R.string.close
                );

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        EditText searchEditText = findViewById(R.id.searchEditText);

        adapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {

                    switch (position) {

                        case 0:
                            tab.setText("Services");
                            tab.setIcon(R.drawable.laundry);
                            break;

                        case 1:
                            tab.setText("Fun");
                            tab.setIcon(R.drawable.carousel);
                            break;

                        case 2:
                            tab.setText("Industry");
                            tab.setIcon(R.drawable.factory);
                            break;

                        case 3:
                            tab.setText("Education");
                            tab.setIcon(R.drawable.university);
                            break;
                    }
                }).attach();

        setupLocationTracking();
        setupViewPager();

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {

                String query =
                        s.toString().toLowerCase().trim();

                // ОВА Е ДОДАДЕНО ЗА ПРЕБАРУВАЊЕТО ДА ФИЛТРИРА ВО ФРАГМЕНТИТЕ
                for (Fragment fragment : fragments) {
                    if (fragment instanceof CompanyFragment) {
                        ((CompanyFragment) fragment).filter(query);
                    }
                }

                if (query.isEmpty()) return;

                for (Company c : allCompanies) {

                    if (c.getName()
                            .toLowerCase()
                            .contains(query)) {

                        int target = -1;

                        if (c.getCategory().contains("Services"))
                            target = 0;

                        else if (c.getCategory().contains("Fun"))
                            target = 1;

                        else if (c.getCategory().contains("Industry"))
                            target = 2;

                        else if (c.getCategory().contains("Education"))
                            target = 3;

                        if (target != -1) {
                            viewPager.setCurrentItem(target, true);
                        }

                        break;
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        NavigationView navigationView =
                findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_services)
                viewPager.setCurrentItem(0);

            else if (id == R.id.nav_fun)
                viewPager.setCurrentItem(1);

            else if (id == R.id.nav_industry)
                viewPager.setCurrentItem(2);

            else if (id == R.id.nav_education)
                viewPager.setCurrentItem(3);

            drawer.closeDrawer(GravityCompat.START);

            return true;
        });

        // BACK BUTTON
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (drawer.isDrawerOpen(GravityCompat.START)) {
                            drawer.closeDrawer(GravityCompat.START);
                        } else {
                            finish();
                        }
                    }
                });
    }

    // NOTIFICATION CHANNEL
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Nearby Companies",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Notifications when near companies"
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    // SEND NOTIFICATION
    private void sendNotification(String companyName) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        200
                );

                return;
            }
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.factory)
                        .setContentTitle("Во близина сте!")
                        .setContentText("Блиску сте до: " + companyName)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this)
                .notify(
                        (int) System.currentTimeMillis(),
                        builder.build()
                );
    }

    // GPS TRACKING
    private void setupLocationTracking() {

        LocationManager locationManager =
                (LocationManager)
                        getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    100
            );

            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                5f,
                new LocationListener() {

                    @Override
                    public void onLocationChanged(
                            @NonNull Location location
                    ) {
                        checkProximity(location);
                    }
                });
    }

    // CHECK DISTANCE
    private void checkProximity(Location userLoc) {

        for (Company company : allCompanies) {

            float[] results = new float[1];

            Location.distanceBetween(
                    userLoc.getLatitude(),
                    userLoc.getLongitude(),
                    company.getLatitude(),
                    company.getLongitude(),
                    results
            );

            if (results[0] <= PROXIMITY_RADIUS) {

                Toast.makeText(
                        this,
                        "Во близина сте на: "
                                + company.getName(),
                        Toast.LENGTH_SHORT
                ).show();

                sendNotification(company.getName());
            }
        }
    }

    // VIEWPAGER
    private void setupViewPager() {

        new Thread(() -> {

            List<Company> dbServices =
                    db.companyDao()
                            .getCompaniesByCategory("Services");

            List<Company> dbFun =
                    db.companyDao()
                            .getCompaniesByCategory("Fun");

            List<Company> dbIndustry =
                    db.companyDao()
                            .getCompaniesByCategory("Industry");

            List<Company> dbEducation =
                    db.companyDao()
                            .getCompaniesByCategory("Education");

            runOnUiThread(() -> {

                fragments.clear();
                allCompanies.clear();

                // SERVICES
                ArrayList<Company> srv = new ArrayList<>();

                srv.add(new Company(
                        "Пералница Експрес",
                        "Кочани",
                        41.918,
                        22.411,
                        "clean@express.mk",
                        "033 111 222",
                        "www.clean.mk",
                        "Services",
                        R.drawable.laundry));

                srv.add(new Company(
                        "Чистота М",
                        "Штип",
                        41.742,
                        22.191,
                        "m@chisto.mk",
                        "032 222 333",
                        "www.chisto.mk",
                        "Services",
                        R.drawable.broom));

                srv.add(new Company(
                        "Авто Сервис Аце",
                        "Велес",
                        41.715,
                        21.772,
                        "ace@auto.mk",
                        "043 555 666",
                        "www.ace.mk",
                        "Services",
                        R.drawable.repair));

                srv.add(new Company(
                        "Coffee Time",
                        "Скопје",
                        41.996,
                        21.433,
                        "coffee@time.mk",
                        "02 444 555",
                        "www.coffee.mk",
                        "Services",
                        R.drawable.coffee));

                srv.add(new Company(
                        "Рент-а-Кар Битола",
                        "Битола",
                        41.029,
                        21.334,
                        "rent@car.mk",
                        "047 111 000",
                        "www.rent.mk",
                        "Services",
                        R.drawable.car));

                if (dbServices != null)
                    srv.addAll(dbServices);

                // FUN
                ArrayList<Company> fun = new ArrayList<>();

                fun.add(new Company(
                        "Кино Синеплекс",
                        "Скопје",
                        42.004,
                        21.393,
                        "kino@fun.mk",
                        "02 999 888",
                        "www.kino.mk",
                        "Fun",
                        R.drawable.cinema));

                fun.add(new Company(
                        "Луна Парк",
                        "Кочани",
                        41.919,
                        22.414,
                        "luna@park.mk",
                        "033 777 666",
                        "www.luna.mk",
                        "Fun",
                        R.drawable.carousel));

                fun.add(new Company(
                        "Болинг Центар",
                        "Куманово",
                        42.135,
                        21.718,
                        "bowl@kumanovo.mk",
                        "031 555 111",
                        "www.bowl.mk",
                        "Fun",
                        R.drawable.bowling));

                fun.add(new Company(
                        "Ноќен Клуб Хавана",
                        "Охрид",
                        41.112,
                        20.801,
                        "disco@ohrid.mk",
                        "046 111 222",
                        "www.disco.mk",
                        "Fun",
                        R.drawable.disco));

                fun.add(new Company(
                        "Играчница Стар",
                        "Струмица",
                        41.437,
                        22.641,
                        "games@strumica.mk",
                        "034 333 444",
                        "www.games.mk",
                        "Fun",
                        R.drawable.games));

                if (dbFun != null)
                    fun.addAll(dbFun);

                // INDUSTRY
                ArrayList<Company> ind = new ArrayList<>();

                ind.add(new Company(
                        "Амфенол Технологии",
                        "Кочани",
                        41.905,
                        22.435,
                        "hr@amphenol.mk",
                        "033 279 000",
                        "www.amphenol.mk",
                        "Industry",
                        R.drawable.factory));

                ind.add(new Company(
                        "Текстилна Фабрика",
                        "Штип",
                        41.745,
                        22.185,
                        "textile@ind.mk",
                        "032 444 111",
                        "www.textile.mk",
                        "Industry",
                        R.drawable.facturyy));

                ind.add(new Company(
                        "Алкалоид",
                        "Скопје",
                        41.999,
                        21.464,
                        "info@alkaloid.mk",
                        "02 3104 000",
                        "www.alkaloid.mk",
                        "Industry",
                        R.drawable.factory));

                ind.add(new Company(
                        "Фени Индустри",
                        "Кавадарци",
                        41.433,
                        22.011,
                        "info@feni.mk",
                        "043 415 100",
                        "www.feni.mk",
                        "Industry",
                        R.drawable.workers));

                ind.add(new Company(
                        "Соларни Системи",
                        "Тетово",
                        42.010,
                        20.971,
                        "sun@power.mk",
                        "044 888 000",
                        "www.sun.mk",
                        "Industry",
                        R.drawable.solar));

                if (dbIndustry != null)
                    ind.addAll(dbIndustry);

                // EDUCATION
                ArrayList<Company> edu = new ArrayList<>();

                edu.add(new Company(
                        "ФИНКИ",
                        "Скопје",
                        42.004,
                        21.409,
                        "finki@ukim.mk",
                        "02 3099 123",
                        "www.finki.mk",
                        "Education",
                        R.drawable.ukim));

                edu.add(new Company(
                        "УГД Економски",
                        "Кочани",
                        41.917,
                        22.415,
                        "info@ugd.mk",
                        "033 222 000",
                        "www.ugd.mk",
                        "Education",
                        R.drawable.ugd));

                edu.add(new Company(
                        "УКЛО Битола",
                        "Битола",
                        41.025,
                        21.333,
                        "info@uklo.mk",
                        "047 223 344",
                        "www.uklo.mk",
                        "Education",
                        R.drawable.uklo));

                edu.add(new Company(
                        "Државен Универзитет",
                        "Тетово",
                        42.011,
                        20.962,
                        "info@unite.edu.mk",
                        "044 356 500",
                        "www.unite.mk",
                        "Education",
                        R.drawable.university));

                edu.add(new Company(
                        "Гимназија Љупчо Сантов",
                        "Кочани",
                        41.916,
                        22.416,
                        "info@lsantov.edu.mk",
                        "033 271 233",
                        "www.lsantov.mk",
                        "Education",
                        R.drawable.hightschool));

                if (dbEducation != null)
                    edu.addAll(dbEducation);

                allCompanies.addAll(srv);
                allCompanies.addAll(fun);
                allCompanies.addAll(ind);
                allCompanies.addAll(edu);

                fragments.add(
                        CompanyFragment.newInstance(
                                "Services",
                                srv
                        ));

                fragments.add(
                        CompanyFragment.newInstance(
                                "Fun",
                                fun
                        ));

                fragments.add(
                        CompanyFragment.newInstance(
                                "Industry",
                                ind
                        ));

                fragments.add(
                        CompanyFragment.newInstance(
                                "Education",
                                edu
                        ));

                adapter.notifyDataSetChanged();
            });

        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(
                R.menu.main_menu,
                menu
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(
            @NonNull MenuItem item
    ) {

        if (item.getItemId() == R.id.action_add) {

            startActivity(
                    new Intent(
                            this,
                            AddCompanyActivity.class
                    )
            );

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViewPager();
    }
}