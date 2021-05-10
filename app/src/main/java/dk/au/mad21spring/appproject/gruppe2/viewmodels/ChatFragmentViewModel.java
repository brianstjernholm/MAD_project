package dk.au.mad21spring.appproject.gruppe2.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.repository.Repository;

public class ChatFragmentViewModel extends ViewModel {

    private Repository repository = null;

    LiveData<List<User>> selectedUsers;

    public void init(Application application) {
        if (repository == null) {
            repository = Repository.getInstance(application);
        }
        //build list of users from current users chats in repo
        initUsersFromChatList();
        //read users from list to selectedUsers
        readSelectedUsers();
    }

    public LiveData<List<Chat>> observeOnChangesInChatList() {
        return repository.getChats();
    }

    public void updateUsersFromChatsList() {
        repository.updateUsersFromChatsList();
    }

    public LiveData<List<User>> getSelectedUsers() {
        return selectedUsers;
    }

    private void initUsersFromChatList() {
        repository.findUsersFromChats();
    }

    public void readSelectedUsers() {
        selectedUsers = repository.getSelectedUsers();
    }
}
