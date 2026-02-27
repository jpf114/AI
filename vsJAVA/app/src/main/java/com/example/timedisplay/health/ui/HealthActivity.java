package com.example.timedisplay.health.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.timedisplay.R;
import com.example.timedisplay.health.database.EncryptionUtil;
import com.example.timedisplay.health.ui.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HealthActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        EncryptionUtil.init(this);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        setupViewPager();
        setupTabLayout();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("健康数据记录");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(new DietFragment(), "饮食");
        adapter.addFragment(new ExerciseFragment(), "运动");
        adapter.addFragment(new SleepFragment(), "睡眠");
        adapter.addFragment(new StatisticsFragment(), "统计");
        viewPager.setAdapter(adapter);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
                    if (adapter != null) {
                        tab.setText(adapter.getPageTitle(position));
                    }
                }
        ).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.health_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_export) {
            viewPager.setCurrentItem(3);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
