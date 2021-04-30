package dk.au.mad21spring.appproject.gruppe2.repository;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21spring.appproject.gruppe2.activities.LoginActivity;
import dk.au.mad21spring.appproject.gruppe2.models.User;

public class Repository {

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
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<List<User>> mUsers = new MutableLiveData<>();



    //Ctor
    public Repository(Application app) {
        executor = Executors.newSingleThreadExecutor();
        context = app.getApplicationContext();
        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
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

    }

    public static Repository getInstance(Application application) {
        if (instance == null) {
            instance = new Repository(application);
        }
        return instance;
    }

    public boolean userLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser().getUid();
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void signOut(Context context) {
        AuthUI.getInstance().signOut(context);
    }

    public LiveData<List<User>> getmUsers() {
        usersReference = FirebaseDatabase.getInstance().getReference("Users");

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.setValue(null);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert  firebaseUser != null;
                    if (!user.getId().equals(firebaseUser.getUid())) {
                        ArrayList<User> temp = new ArrayList<>();
                        temp.add(user);
                        mUsers.setValue(temp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return mUsers;
    }

    public void registerUserToDb(Activity activity) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

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
}
