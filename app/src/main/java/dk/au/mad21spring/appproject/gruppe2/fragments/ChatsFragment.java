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

import java.util.ArrayList;
import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.adapters.UserAdapter;
import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.ChatFragmentViewModel;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.UserFragmentsViewModel;


public class ChatsFragment extends Fragment {

    //UI
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;

    private ChatFragmentViewModel vm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setUpViewModel();

        //setting up temporary list for initialization of userAdapter
        List<User> mUsers = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(getContext(), mUsers, false);
        recyclerView.setAdapter(userAdapter);

        return view;
    }

    private void setUpViewModel() {
        vm = new ViewModelProvider(this).get(ChatFragmentViewModel.class);
        vm.init(getActivity().getApplication());

        //Setting up observers
        //First observer updates recyclerview
        vm.getSelectedUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> chatUsers) {
                userAdapter.updateData(chatUsers); //sending new data to adapter
                userAdapter.notifyDataSetChanged(); //updating recyclerview/adapter with the new data
            }
        });

        //Second observer watches for changes in list of chats in repo
        //upon changes the viewmodel is udpated which triggers first observer
        vm.observeOnChangesInChatList().observe(getViewLifecycleOwner(), new Observer<List<Chat>>() {
            @Override
            public void onChanged(List<Chat> chats) {
                vm.updateUsersFromChatsList();
                vm.readSelectedUsers();
            }
        });
    }
}