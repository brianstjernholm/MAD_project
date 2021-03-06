package dk.au.mad21spring.appproject.gruppe2.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.adapters.ViewPagerAdapter;
import dk.au.mad21spring.appproject.gruppe2.fragments.ChatsFragment;
import dk.au.mad21spring.appproject.gruppe2.fragments.UsersFragment;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.services.NotificationsService;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.ProfileViewModel;

//Adapted from https://www.youtube.com/watch?v=LyAmpfm4ndo&list=PLzLFqCABnRQftQQETzoVMuteXzNiXmnj8&index=3
public class ProfileActivity extends AppCompatActivity {

    //UI widgets
    CircleImageView profile_image;
    TextView username;

    private ProfileViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Setting up activity
        setUpViewModel();
        startService();
        setupToolbar();

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        //get current user info from db and set view
        User user = vm.getCurrentUserFromDb();
        username.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
        }

        setupTablayout();
    }

    private void startService() {
        //Initializing notification service
        Intent notificationServiceIntent = new Intent(this, NotificationsService.class);
        startService(notificationServiceIntent);
    }

    private void setupTablayout() {
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        //Setting up ViewPageAdapter and adding fragments
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new ChatsFragment(), getResources().getString(R.string.titleMyChats));
        viewPagerAdapter.addFragment(new UsersFragment(), getResources().getString(R.string.titleUsers));

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
    }

    private void setUpViewModel() {
        vm = new ViewModelProvider(this).get(ProfileViewModel.class);
        vm.init(getApplication());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //Signout method
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:
                vm.signOut(this);
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
                return true;
        }
        return false;
    }

}