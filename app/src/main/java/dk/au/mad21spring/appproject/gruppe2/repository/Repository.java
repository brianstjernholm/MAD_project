package dk.au.mad21spring.appproject.gruppe2.repository;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Region;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.models.ChuckNorris;
import dk.au.mad21spring.appproject.gruppe2.models.ChuckParser;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.utils.Constants;

public class Repository {
    //Variables
    private ExecutorService executor;                   //service for async processing
    private RequestQueue queue;                         //volley requestqueue
    private Context context;
    private static Repository instance;

    //Firebase
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference chatsReference;
    DatabaseReference usersReference;

    // Lists for accessing and observing data from vm/activities
    private String userImageUrl = "default";

    //general purpose lists
    private MutableLiveData<List<User>> userList;
    private MutableLiveData<List<Chat>> chatList;

    //list for chatfragment
    private MutableLiveData<List<User>> usersFromChats;

    //list for userfragment
    private MutableLiveData<List<User>> listWithoutCurrentUser;

    //list for MessageAcitivty
    private MutableLiveData<List<Chat>> mChats = new MutableLiveData<>();

    //notification service
    private MutableLiveData<String> latestChatSenderUid;
    private MutableLiveData<ChuckNorris> newChuck;


    //CTOR
    public Repository(Application app) {
        executor = Executors.newSingleThreadExecutor();
        context = app.getApplicationContext();
        context = app.getApplicationContext();

        auth = FirebaseAuth.getInstance();

        userList = new MutableLiveData<>();
        chatList = new MutableLiveData<>();

        listWithoutCurrentUser = new MutableLiveData<>();
        usersFromChats = new MutableLiveData<>();
        latestChatSenderUid = new MutableLiveData<>();
        newChuck = new MutableLiveData<>();

        readUsers();
        readChats();
    }


    public static Repository getInstance(Application application) {
        if (instance == null) {
            instance = new Repository(application);
        }
        return instance;
    }

    // GETTING LIST OF ALL USERS FROM DB
    //reading user from db to userList and setting up observer
    private void readUsers() {
        usersReference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> temp = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    temp.add(user);
                }
                userList.postValue(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    //Method fro observing on changes in list of all users (will update with new entry in db)
    public LiveData<List<User>> getUsers() {
        return userList;
    }


    // GETTING LIST OF ALL CHATS FROM DB
    //reading chats from db to chatList and setting up observer
    private void readChats() {
        String uid;
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else{
            uid = "default";
        }

        chatsReference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Chats");

        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Chat> tempList = new ArrayList<>();
//                String chatTemp = new String();
                Chat tempChat = new Chat();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    tempList.add(chat);
                    tempChat = chat;
//                    chatTemp = chat.getReceiver();
                }
                chatList.postValue(tempList);

//                if (chatTemp.equals(uid))
//                latestChatSenderUid.postValue(chatTemp);

                if (tempChat.getReceiver().equals(uid)){
                    latestChatSenderUid.setValue(tempChat.getSender());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    // Method for chatfragment and MessageActivity to observe on changes in list of all chats (will update with new entry in db)
    public LiveData<List<Chat>> getChats() {
        return chatList;
    }


    // Method for notificationservice and chatfragment to observe on id of last user to send chat
    public LiveData<String> observeOnLatestChat(){
        return latestChatSenderUid;
    }


    //Register user to db (upon successful registration)
    // TODO check if user is owerwriten or if we need to make db
    public void registerUserToDb(Activity activity) {
        //Getting current user (logged in user)
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        //Evt log.d
        String userid = firebaseUser.getUid();

        //Get specific reference to database
        DatabaseReference reference = usersReference.child(userid);

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
                    Toast.makeText(activity, activity.getResources().getString(R.string.successfullRegistration), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    //region Methods for initializing and updating list of all users without current user (for userfragment)

    //Method fro initializing list of all users without current user
    public void initUserListWithoutCurrentUser() {
        List<User> temp = new ArrayList<>();

        if (userList!=null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            for (User user : userList.getValue()) {
                if (!user.getId().equals(uid)) {
                    temp.add(user);
                }
            }
        }
        listWithoutCurrentUser.postValue(temp);
    }
    // Method for userfragment to observe on list of all users without current user
    public LiveData<List<User>> getUserListWithoutCurrentUser() {
        return listWithoutCurrentUser;
    }

    // Method for udating list of all users without current user
    public void updateUserListWithoutCurrentUser() {
        initUserListWithoutCurrentUser();
    }

    //endregion



    //region Methods for handling list of user with whom current user have chatted (usersFromChat)

    // Method for chatfragment to listen on changes in list of user with whom I've chatted with
    public LiveData<List<User>> getSelectedUsers() {
        return usersFromChats;
    }

    //Methods for initializing and updating list og users with whom I've chatted with
    public void updateUsersFromChatsList() {
        findUsersFromChats();
    }

    public void findUsersFromChats() {
        if (chatList!=null) {
            List<String> userIdList = getUserIdList();
            List<User> temp = new ArrayList<>();

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
        String uid = firebaseUser.getUid();
        List<String> userIdList = new ArrayList<>();
        List<Chat> cl = chatList.getValue();

        for (Chat chat : cl) {
            if (chat.getSender()!=null && chat.getSender().equals(uid) && !userIdList.contains(chat.getReceiver())) {
                userIdList.add(chat.getReceiver());
            }
            if (chat.getReceiver()!= null && chat.getReceiver().equals(uid) && !userIdList.contains(chat.getSender())) {
                userIdList.add(chat.getSender());
            }
        }

        return userIdList;
    }

    //endregion



    //region Auth related Methods

    ////////// Auth/Login related ////////////
    public boolean userLoggedIn() { return auth.getCurrentUser() != null; }

    public void signOut(Context context) { AuthUI.getInstance().signOut(context); }

    public String getCurrentUserId() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseUser.getUid();
    }

    //endregion



    //region Methods for getting users from list of users
    ////////// Db related ////////////
    //Get current user from db and setup listener
    public User getCurrentUserFromDb() { //LiveData<User>
        //Getting current user from firebase (logged in user)
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        User tempUser = new User("default", "default", "default");

        for (User user : userList.getValue()) {
            if (user.getId().equals(fbUser.getUid())) {
                tempUser = user;
            }
        }
        return tempUser;
    }

    //Get user from db and setup listener
    public User getUserFromDb(String uid) {//LiveData<User>
        User tempUser = new User("default", "default", "default");

        for (User user : userList.getValue()) {
            if (user.getId().equals(uid)) {
                tempUser = user;
            }
        }
        return tempUser;
    }

    //endregion



    //region Messaging methods
    ////////// Code for sending messages //////////
    public void sendMessage(String receiver, String message) {
        //Getting sender/current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String sender = new String();
        if (firebaseUser != null){
            sender = firebaseUser.getUid();
        }

        //Building message
        HashMap<String, String> hashmap = new HashMap<>();
        hashmap.put("sender", sender);
        hashmap.put("receiver", receiver);
        hashmap.put("message", message);

        chatsReference.push().setValue(hashmap);
    }


    ////////////// Code for reading messages /////////////////////
    public LiveData<List<Chat>> readMessages(String uid) {
        return mChats;
    }

    public void updateMyMessages(String uid) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        List<Chat> tempChat = new ArrayList<>();

        for (Chat chat : chatList.getValue()) {
            if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(uid) ||
                    chat.getReceiver().equals(uid) && chat.getSender().equals(firebaseUser.getUid())) {
                tempChat.add(chat);
            }
        }
        mChats.setValue(tempChat);
    }

    //endregion



    //region API methods

    //API RELATED
    //Observer for notificationservice
    public LiveData<ChuckNorris> observeOnChuck() {
        return newChuck;
    }

    //Get a chuck joke
    public void getChuck() {
        String dataUrl = "https://api.chucknorris.io/jokes/random";
        sendApiRequest(dataUrl);
    }

    //Send API request using volley
    private void sendApiRequest(String dataUrl) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (queue == null){
                    queue = Volley.newRequestQueue(context);
                }

                StringRequest request = new StringRequest(Request.Method.GET, dataUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.API_TAG, "Chuck joke generated: " + response);
                        parseJson(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(Constants.API_TAG, "No Chuck", error);
                        errormessage_noJoke();
                    }
                });
                queue.add(request);
            }
        });
    }

    //Parse the API json response to own class
    private void parseJson(String json) {
        Gson gson = new GsonBuilder().create();
        ChuckParser temporaryChuck = gson.fromJson(json, ChuckParser.class);

        if (temporaryChuck!=null) {
            ChuckNorris chuck = new ChuckNorris(
                    temporaryChuck.getCreatedAt(),
                    temporaryChuck.getIconUrl(),
                    temporaryChuck.getId(),
                    temporaryChuck.getUpdatedAt(),
                    temporaryChuck.getUrl(),
                    temporaryChuck.getValue()
            );
            newChuck.postValue(chuck);
        }
    }

    //endregion


    public String getImageURL(String userid) {
        for (User user : userList.getValue()) {
            if (user.getId().equals(userid)) {
                return user.getImageURL();
            }
        }
        return "Default";
    }

    private void errormessage_noJoke() {
        Toast.makeText(context, context.getResources().getString(R.string.nojoke), Toast.LENGTH_SHORT).show();
    }
}
