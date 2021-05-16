package dk.au.mad21spring.appproject.gruppe2.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;

import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.utils.Constants;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    //UI Widgets
    Button btnSignIn;

    private LoginViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUpViewModel();

        btnSignIn = findViewById(R.id.btnSignin);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void setUpViewModel() {
        vm = new ViewModelProvider(this).get(LoginViewModel.class);
        vm.init(getApplication());
    }

    private void signIn() {
        // If user is already logged in go directly to profile
        // otherwise build list of external providers and and start login activity
        if (vm.userLoggedIn()){
            goToProfile();
        } else {
            List<AuthUI.IdpConfig> providers = vm.buildExternalProviderList();
            // go to external auth
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    Constants.REQUEST_LOGIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // when returning from external auth: check result, register user and go to profile view
        if (requestCode == Constants.REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                String uid = vm.getCurrentUserId();
                vm.registerUserToDb(LoginActivity.this);

                Toast.makeText(this, getResources().getString(R.string.successfulLogin) +"\n" + uid, Toast.LENGTH_SHORT).show();

                goToProfile();
            }
        }
    }

    private void goToProfile() {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
        finish();
    }
}