package dk.au.mad21spring.appproject.gruppe2.repository;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.RequestQueue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    //User user;
    private MutableLiveData<User> user = new MutableLiveData<>();


    //Ctor
    public Repository(Application app) {
        executor = Executors.newSingleThreadExecutor();
        context = app.getApplicationContext();
        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user.setValue( snapshot.getValue(User.class) );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

}
