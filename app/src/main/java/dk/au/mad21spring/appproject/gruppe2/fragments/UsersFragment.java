package dk.au.mad21spring.appproject.gruppe2.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
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
import dk.au.mad21spring.appproject.gruppe2.adapters.ChatAdapter;
import dk.au.mad21spring.appproject.gruppe2.adapters.UserAdapter;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.ProfileViewModel;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.UserFragmentsViewModel;
//Adapted from https://www.youtube.com/watch?v=LyAmpfm4ndo&list=PLzLFqCABnRQftQQETzoVMuteXzNiXmnj8&index=3

//Handles displaying list of all app users
public class UsersFragment extends Fragment {

    private UserFragmentsViewModel vm;
    private RecyclerView recyclerView;

    private UserAdapter userAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setUpViewModel();

        // temporary list for loading recyclerview
        List<User> mUsers = new ArrayList<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //Setting up recyclerview
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(getContext(), mUsers);
        recyclerView.setAdapter(userAdapter);

        return view;
    }


    private void setUpViewModel() {
        vm = new ViewModelProvider(this).get(UserFragmentsViewModel.class);
        vm.init(getActivity().getApplication());

        //Observe on list of users
        // -> if new user then update vm and check if relevant
        // -> update list of users with whom current user has chat
        vm.getListOfUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                vm.update();
                vm.readList();
            }
        });

        //Observes on changes to list of users with whom current user has chat and updates recyclerview
        vm.getSelectedUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                userAdapter.updateData(users);
                userAdapter.notifyDataSetChanged();
            }
        });
    }
}