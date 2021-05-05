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
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.utils.Constants;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button btnSignIn;
    private LoginViewModel vm;

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (vm.userLoggedIn()) {
//            goToProfile();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //auth = FirebaseAuth.getInstance();

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
        // if(vm.userLoggedIn) -> go to profile
        // else vm.signIn()

        if (vm.userLoggedIn()){
            goToProfile();
        } else {
            List<AuthUI.IdpConfig> providers = vm.buildExternalProviderList();
//            List<AuthUI.IdpConfig> providers = Arrays.asList(
//                    new AuthUI.IdpConfig.EmailBuilder().build(),
//                    //new AuthUI.IdpConfig.FacebookBuilder().build(),
//                    new AuthUI.IdpConfig.TwitterBuilder().build(),
//                    new AuthUI.IdpConfig.GoogleBuilder().build()
//            );

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

        if (requestCode == Constants.REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                String uid = vm.getCurrentUserId();
                vm.registerUserToDb(LoginActivity.this);
                //String uid = auth.getCurrentUser().getUid();
                Toast.makeText(this, getResources().getString(R.string.successfulLogin) +"\n" + uid, Toast.LENGTH_SHORT).show(); //"User logged in"

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