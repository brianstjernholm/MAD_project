package dk.au.mad21spring.appproject.gruppe2.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.repository.Repository;

public class ProfileViewModel extends ViewModel {
    private Repository repository = null;

    public void init(Application application) {
        if (repository == null) {
            repository = Repository.getInstance(application);
        }
    }

    public User getCurrentUserFromDb() {
        return repository.getCurrentUserFromDb();
    }

    public void signOut(Context context) {
        repository.signOut(context);
    }
}
