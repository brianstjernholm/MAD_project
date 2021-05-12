package dk.au.mad21spring.appproject.gruppe2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.adapters.MessageAdapter;
import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.MessageViewModel;

public class MessageActivity extends AppCompatActivity {

    //UI widgets
    CircleImageView profile_image;
    TextView username;
    ImageButton btn_send;
    EditText text_send;
    Context context;

    MessageAdapter messageAdapter;

    RecyclerView recyclerView;
    private MessageViewModel vm;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        context = this;

        setupViewModel();
        setupToolbar();
        setupUI();
        setupRecyclerView();

        intent = getIntent();
        String userid = intent.getStringExtra("userid");

        //Getting selected user from db and setting up view
        User user = vm.getUserFromDb(userid);

        username.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
        }

        //getting chats to/from user and setting up observer
        readMessages(userid);

        //Setting up send button (send message)
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(userid, msg);
                } else {
                    makeToast_sendMessageError();
                }
                text_send.setText("");
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setupUI() {
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
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

    private void readMessages(final String userid) {
        vm.readMessages(userid).observe(this, new Observer<List<Chat>>() {
            @Override
            public void onChanged(List<Chat> chats) {
                messageAdapter = new MessageAdapter(MessageActivity.this, chats, vm.getImageUrl(userid)); //vm.getImageUrl(userid) //userImageUrl
                recyclerView.setAdapter(messageAdapter);
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void makeToast_sendMessageError() {
        Toast.makeText(this, getResources().getString(R.string.btnSendErrorMessage), Toast.LENGTH_SHORT).show();
    }
}