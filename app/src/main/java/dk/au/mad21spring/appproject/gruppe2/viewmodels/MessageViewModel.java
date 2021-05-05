package dk.au.mad21spring.appproject.gruppe2.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.repository.Repository;

public class MessageViewModel extends ViewModel {
    private Repository repository = null;
    private String uid;

    public void init(Application application) {
        if (repository == null) {
            repository = Repository.getInstance(application);
        }
    }

    public LiveData<User> getUserFromDb(String uid) { return repository.getUserFromDb(uid); }

    public void signOut(Context context) {
        repository.signOut(context);
    }

    public void sendMessage(String receiver, String message) {
        repository.sendMessage(receiver, message);
    }

    public LiveData<List<Chat>> readMessages(String uid) {
        return repository.readMessages(uid);
    }

    public String getImageUrl(String userid) {
        return repository.getImageUrl(userid);
    }

}
