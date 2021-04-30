package dk.au.mad21spring.appproject.gruppe2.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.adapters.UserAdapter;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.ProfileViewModel;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.UserFragmentsViewModel;

public class UsersFragment extends Fragment {

    private UserFragmentsViewModel vm;
    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    //private List<User> mUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setUpViewModel();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //mUsers = new ArrayList<>();

        readUsers();

        vm.getmUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                userAdapter = new UserAdapter(getContext(), users, false);
                recyclerView.setAdapter(userAdapter);
            }
        });

        return view;
    }

    private void readUsers() {
        //mUsers = vm.getmUsers()
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
    }

    private void setUpViewModel() {
        vm = new ViewModelProvider(this).get(UserFragmentsViewModel.class);
        vm.init(getActivity().getApplication());
    }
}