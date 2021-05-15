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

    //initialize the list of users without current user in repo
    private void initUsersFromChatList() {
        repository.findUsersFromChats();
    }

    //read data from repo to local selectedUserList
    //This triggers the observer in the fragment
    public void readSelectedUsers() {
        selectedUsers = repository.getSelectedUsers();
    }

    //Observes on the list of all chats in repo and notifies fragment when a new chat is added
    public LiveData<List<Chat>> observeOnChangesInChatList() {
        return repository.getChats();
    }

    //updates the list of selected users in repo when a new chat is registered
    public void updateUsersFromChatsList() {
        repository.updateUsersFromChatsList();
    }

    //Observes on the list of selected users in repo and notifies fragment when the list is updated
    public LiveData<List<User>> getSelectedUsers() {
        return selectedUsers;
    }

    //notificationservice!!!
    public LiveData<String> observeOnLatestChat() {
        return repository.observeOnLatestChat();
    }


}
