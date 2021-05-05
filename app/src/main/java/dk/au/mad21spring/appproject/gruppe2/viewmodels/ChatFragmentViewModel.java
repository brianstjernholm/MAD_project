package dk.au.mad21spring.appproject.gruppe2.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.repository.Repository;

public class ChatFragmentViewModel extends ViewModel {
    private Repository repository = null;
    private String uid;

    public void init(Application application) {
        if (repository == null) {
            repository = Repository.getInstance(application);
        }
    }

    public LiveData<List<String>> getListOfUsersIdsFromMyChats() {
        return repository.getListOfUsersIdsFromMyChats();
    }
}
