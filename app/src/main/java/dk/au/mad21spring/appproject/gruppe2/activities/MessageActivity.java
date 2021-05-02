package dk.au.mad21spring.appproject.gruppe2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.icu.text.StringSearch;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import de.hdodenhof.circleimageview.CircleImageView;
import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.MessageViewModel;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.ProfileViewModel;

public class MessageActivity extends AppCompatActivity {

    //UI widgets
    CircleImageView profile_image;
    TextView username;
    ImageButton btn_send;
    EditText text_send;

    private MessageViewModel vm;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        setupViewModel();

        setupToolbar();

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        String userid = intent.getStringExtra("userid");

        //Setting up ui and listening for changes
        vm.getUserFromDb(userid).observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
            }
        });

        //Setting up send button (send message)
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });
    }

    private void setupToolbar() {
        //Setting up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setupViewModel() {
        //Setting up ViewModel
        vm = new ViewModelProvider(this).get(MessageViewModel.class);
        vm.init(getApplication());
    }

    private void sendMessage(String receiver, String message) {
        //Sender is always current user and is handled in repo
        vm.sendMessage(receiver, message);
    }
}