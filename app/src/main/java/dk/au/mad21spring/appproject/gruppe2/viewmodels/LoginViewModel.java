package dk.au.mad21spring.appproject.gruppe2.viewmodels;

import android.app.Activity;
import android.app.Application;

import androidx.lifecycle.ViewModel;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.repository.Repository;

public class LoginViewModel extends ViewModel {

    private Repository repository = null;

    public void init(Application application) {
        if (repository == null) {
            repository = Repository.getInstance(application);
        }
    }

    public boolean userLoggedIn() {
        return repository.userLoggedIn();
    }

    public List<AuthUI.IdpConfig> buildExternalProviderList() {
        return Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build() //Currently not working
        );
    }

    public String getCurrentUserId() {
        return repository.getCurrentUserId();
    }

    public void registerUserToDb(Activity activity) {
        repository.registerUserToDb(activity);
    }
}
