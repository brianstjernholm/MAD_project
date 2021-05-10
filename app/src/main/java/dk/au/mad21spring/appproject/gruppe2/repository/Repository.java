package dk.au.mad21spring.appproject.gruppe2.repository;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.RequestQueue;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.models.User;

public class Repository {
    //SAFETY VERSION BUT
    //Variables
    private ExecutorService executor;                   //service for async processing
    private RequestQueue queue;                         //volley requestqueue
    private Context context;
    private static Repository instance;
    //Firebase
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    DatabaseReference usersReference;
    //User user;
    // Lists for accessing and observing data from vm/activities
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<List<User>> mUsers = new MutableLiveData<>();
    private List<String> chatUserList;
    private MutableLiveData<List<Chat>> mChats = new MutableLiveData<>();
    private User userForRetrievingImageUrl = new User();
    private MutableLiveData<List<User>> chatUsers;
    private String userImageUrl = "default";


    //general purpose lists
    private MutableLiveData<List<User>> userList;
    private MutableLiveData<List<Chat>> chatList;

    //list for chatfragment
    private MutableLiveData<List<User>> usersFromChats;

    //CTOR
    public Repository(Application app) {
        executor = Executors.newSingleThreadExecutor();
        context = app.getApplicationContext();
        auth = FirebaseAuth.getInstance();
        chatUserList = new ArrayList<>();
        //readUsersFromDb();
        chatUsers = new MutableLiveData<>();

        //Get current user
        //firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        userList = new MutableLiveData<>();
        chatList = new MutableLiveData<>();

        usersFromChats = new MutableLiveData<>();

        readUsers();
        readChats();
    }


    public static Repository getInstance(Application application) {
        if (instance == null) {
            instance = new Repository(application);
        }
        return instance;
    }

    //reading user from db to userList and setting up observer
    private void readUsers() {
        usersReference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //mUsers.setValue(null);
                ArrayList<User> temp = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    temp.add(user);

//                    assert firebaseUser != null;
//                    if (!user.getId().equals(firebaseUser.getUid())) {
//                        temp.add(user);
//                    }
                }
                userList.postValue(temp);
                //findUsersFromChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //reading chats from db to chatList and setting up observer
    private void readChats() {
        usersReference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Chats");

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //mUsers.setValue(null);
                ArrayList<Chat> temp = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    temp.add(chat);
                }
                chatList.postValue(temp);
                //findUsersFromChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //getters/methods for observing on livedata lists for activites/vm
    public LiveData<List<User>> getSelectedUsers() {
        return usersFromChats;
    }

    public LiveData<List<Chat>> getChats() {
        return chatList;
    }

    //Methods for initializing and updating chatlist in chatfragment
    public void updateUsersFromChatsList() {
        findUsersFromChats();
    }

    public void findUsersFromChats() {
        if (chatList!=null) {
            List<String> userIdList = getUserIdList();
            List<User> temp = new ArrayList<>();
            List<User> ul = usersFromChats.getValue();

            //for every user and very id in list:
            for (User user : userList.getValue()) {
                for (String id : userIdList) {
                    //if there is a match
                    if (user.getId().equals(id)) {
                        //Check if list is empty
                        if (temp.size()!=0) {
                            if (!temp.contains(user.getId())){
                                temp.add(user);
                            }
                        } else {
                            temp.add(user);
                        }

                    }
                }
            }
            usersFromChats.postValue(temp);
        }
    }

    private List<String> getUserIdList(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        List<String> userIdList = new ArrayList<>();
        List<Chat> cl = chatList.getValue();
        for (Chat chat : cl) {
            if (chat.getSender().equals(firebaseUser.getUid()) && !userIdList.contains(chat.getReceiver())) {
                userIdList.add(chat.getReceiver());
            }
            if (chat.getReceiver().equals(firebaseUser.getUid()) && !userIdList.contains(chat.getReceiver())) {
                userIdList.add(chat.getSender());
            }
        }
        return userIdList;
    }


    ////////// Auth related ////////////
    public boolean userLoggedIn() { return auth.getCurrentUser() != null; }

    public void signOut(Context context) { AuthUI.getInstance().signOut(context); }

    public String getCurrentUserId() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseUser.getUid();
        //return auth.getCurrentUser().getUid();
    }

//    public FirebaseUser getCurrentUser() {
//        return FirebaseAuth.getInstance().getCurrentUser();
//    }




    ////////// Db related ////////////
    //Get current user from db and setup listener
    public LiveData<User> getCurrentUserFromDb() {
        //Getting current user from firebase (logged in user)
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser!=null) {
            reference = FirebaseDatabase
                    .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("Users")
                    .child(fbUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUser.setValue(snapshot.getValue(User.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return currentUser;
    }

    //Get user from db and setup listener
    public LiveData<User> getUserFromDb(String uid) {
        //Getting current user from firebase (logged in user)
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser!=null) {
            reference = FirebaseDatabase
                    .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("Users")
                    .child(uid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user.setValue(snapshot.getValue(User.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return user;
    }


    //Register user to db upon successful registration
    // TODO check if user is owerwriten or if we need to make db
    public void registerUserToDb(Activity activity) {
        //Getting current user (logged in user)
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users")
                .child(userid);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", userid);
        hashMap.put("username", firebaseUser.getDisplayName());
        hashMap.put("imageURL", "default");
        hashMap.put("status", "offline");
        hashMap.put("search", firebaseUser.getDisplayName());

        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(activity, "You can't register woth this email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    ////////// Code for user fragment (RecyclerView) ////////////
    private void readUsersFromDb() {
        //Getting current user (logged in user)
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersReference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //mUsers.setValue(null);
                ArrayList<User> temp = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert firebaseUser != null;
                    if (!user.getId().equals(firebaseUser.getUid())) {
                        temp.add(user);
                    }
                }
                mUsers.setValue(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<List<User>> getListOfUsers() {
        readUsersFromDb();
        return  mUsers;
    }

    ////////// Code for sending messages //////////
    public void sendMessage(String receiver, String message) {
        //Getting sender/current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String sender = new String();
        if (firebaseUser != null){
            sender = firebaseUser.getUid();
        }

        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference();

        //Building message
        HashMap<String, String> hashmap = new HashMap<>();
        hashmap.put("sender", sender);
        hashmap.put("receiver", receiver);
        hashmap.put("message", message);

        reference.child("Chats").push().setValue(hashmap);

    }

    //////////// Code for building chat fragment ////////////
    public LiveData<List<User>> readUsersFromCurrentUserChats(){
        readUserIdsFromChats(); // gets the Ids from current users chats (to/from) and sets chatUserList
        //readChatUsers(); // find users from ids and sets chatUsers
        return chatUsers;
    }

    private void readChatUsers() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference chatUsersReference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
        chatUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> temp = new ArrayList<>();
                if (chatUsers.getValue()!=null) {
                    temp = chatUsers.getValue();
                }

                //Iterator<User> iter = temp.iterator();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //chatUsers.getValue().clear();
                    User user = snapshot.getValue(User.class);

                    //Find users with whom current user have correspondence
                    for (String id : chatUserList) {
                        if (user.getId().equals(id)) {
                            //Check if list is empty
                            if (temp.size() != 0) {
                                //Check if user already exists in list
                                for (User user1 : temp) {
                                    if (!user.getId().equals(user1.getId())) {
                                        temp.add(user);
                                    }
                                }
                            } else {
                                temp.add(user);
                            }
                        }
                    }
                }
                chatUsers.setValue(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void readUserIdsFromChats() {
        //Getting all users()receivers/senders from current users chats
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference chatUsersReference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Chats");
        chatUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatUserList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getSender().equals(firebaseUser.getUid())) {
                        chatUserList.add(chat.getReceiver());
                    }
                    if (chat.getReceiver().equals(firebaseUser.getUid())) {
                        chatUserList.add(chat.getSender());
                    }
                }
                readChatUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ////////////// Code for reading messages /////////////////////
    public LiveData<List<Chat>> readMessages(String uid) {
        readMessagesFromDb(uid);
        return mChats;
    }

    public void readMessagesFromDb(String uid) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Chat> temp = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    assert chat != null;
                    assert firebaseUser != null;
                    //Selecting chats between currentuser and selected user(id)
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(uid) ||
                            chat.getReceiver().equals(uid) && chat.getSender().equals(firebaseUser.getUid())) {
                        temp.add(chat);
                    }
                }
                mChats.setValue(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public String getImageUrl(String userid){

        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users")
                .child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //userForRetrievingImageUrl = snapshot.getValue(User.class);
                userImageUrl = snapshot.getValue(User.class).getImageURL();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //return userForRetrievingImageUrl.getImageURL();
        return userImageUrl;
    }
}
