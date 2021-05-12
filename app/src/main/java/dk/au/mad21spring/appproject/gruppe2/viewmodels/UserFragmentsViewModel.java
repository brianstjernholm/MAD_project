package dk.au.mad21spring.appproject.gruppe2.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.repository.Repository;

public class UserFragmentsViewModel extends ViewModel {
    private Repository repository = null;
    public LiveData<List<User>> selectedUsers;

    public void init(Application application) {
        if (repository == null) {
            repository = Repository.getInstance(application);
        }

        initList();
        readList();
    }

    //initialize the list of users without current user in repo
    public void initList() {
        repository.initUserListWithoutCurrentUser();
    }

    //read data from repo to local selectedUserList
    //This triggers the observer in the fragment
    public void readList() {
        selectedUsers = repository.getUserListWithoutCurrentUser();
    }

    //Observes on the list of all users in repo and notifies fragment when a new user is added
    public LiveData<List<User>> getListOfUsers() {
        return repository.getUsers();
    }

    //updates the list of users without current user in repo when a new user is registered
    public void update() {
        repository.updateUserListWithoutCurrentUser();
    }

    //Notifies the fragment of changes to the userlist so recyclerview can be updated
    public LiveData<List<User>> getSelectedUsers() {
        return selectedUsers;
    }
}
