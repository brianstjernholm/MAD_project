package dk.au.mad21spring.appproject.gruppe2.repository;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
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
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.utils.Constants;

public class Repository {
    //UPDATED VERSION
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
    private MutableLiveData<List<Chat>> mChats = new MutableLiveData<>();
    private String userImageUrl = "default";

    //general purpose lists
    private MutableLiveData<List<User>> userList;
    private MutableLiveData<List<Chat>> chatList;

    //list for chatfragment
    private MutableLiveData<List<User>> usersFromChats;

    //list for userfragment
    private MutableLiveData<List<User>> listWithoutCurrentUser;

    //notification service
    private MutableLiveData<String> latestChatSenderUid;

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

    public LiveData<List<User>> getUsers() {
        return userList;
    }

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

    public void updateUserListWithoutCurrentUser() {
        initUserListWithoutCurrentUser();
    }


    public LiveData<List<User>> getUserListWithoutCurrentUser() {
        return listWithoutCurrentUser;
    }

    //reading chats from db to chatList and setting up observer
    private void readChats() {
        chatsReference = FirebaseDatabase
                .getInstance("https://family-group-7b6dc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Chats");

        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Chat> tempList = new ArrayList<>();
                //Chat chatTemp = new Chat();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    tempList.add(chat);
                    //chatTemp = chat;
                }
                chatList.postValue(tempList);
                //latestChat.postValue(chatTemp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    //getters/methods for activites/vm for observing on livedata lists
    public LiveData<List<User>> getSelectedUsers() {
        return usersFromChats;
    }

    public LiveData<List<Chat>> getChats() {
        return chatList;
    }

    //notificationservice
    public LiveData<String> observeOnLatestChat(){
        return latestChatSenderUid;
    }

    //Methods for initializing and updating chatlist in chatfragment
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

//        Iterator<Chat> clIterator = cl.iterator();
//        while (clIterator.hasNext()) {
//            if (clIterator.next().getSender().equals(uid) && !userIdList.contains(clIterator.next().getReceiver())) {
//                userIdList.add(clIterator.next().getReceiver());
//            }
//            if (clIterator.next().getReceiver().equals(uid) && !userIdList.contains(clIterator.next().getSender())) {
//                userIdList.add(clIterator.next().getSender());
//            }
//            clIterator.next();
//        }

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



    ////////// Auth/Login related ////////////
    public boolean userLoggedIn() { return auth.getCurrentUser() != null; }

    public void signOut(Context context) { AuthUI.getInstance().signOut(context); }

    public String getCurrentUserId() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseUser.getUid();
    }



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


    //Register user to db upon successful registration
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

        //reference.child("Chats").push().setValue(hashmap);
        chatsReference.push().setValue(hashmap);

        if (receiver.equals(firebaseUser.getUid())) {
            latestChatSenderUid.postValue(sender);
        }

    }


    ////////////// Code for reading messages /////////////////////
    //TODO: needs updating
    public LiveData<List<Chat>> readMessages(String uid) {
        readMessagesFromDb(uid);
        return mChats;
    }

    //TODO: needs updating
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

    //TODO: needs updating
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

    //Get API data
    public void getChuck() {
        String dataUrl = "https://api.chucknorris.io/jokes/random";
        sendApiRequest(dataUrl);
    }

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
                        Toast.makeText(context, "Sorry. No joke could be found.", Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(request);
            }
        });
    }

    private void parseJson(String json) {
        Gson gson = new GsonBuilder().create();
        ChuckNorris chuck = gson.fromJson(json, ChuckNorris.class);

        if (chuck.getValue()==null){
            Toast.makeText(context, "Chuck wasn't found", Toast.LENGTH_SHORT).show();
        }else{

        }
    }
}
