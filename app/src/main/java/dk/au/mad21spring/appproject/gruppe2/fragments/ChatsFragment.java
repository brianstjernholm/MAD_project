package dk.au.mad21spring.appproject.gruppe2.fragments;

import android.content.Context;
import android.content.Intent;
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
import dk.au.mad21spring.appproject.gruppe2.adapters.ChatAdapter;
import dk.au.mad21spring.appproject.gruppe2.adapters.UserAdapter;
import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.services.NotificationsService;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.ChatFragmentViewModel;


public class ChatsFragment extends Fragment {

    //UI
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;

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

        chatAdapter = new ChatAdapter(getContext(), mUsers, false);
        recyclerView.setAdapter(chatAdapter);

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
                chatAdapter.updateData(chatUsers); //sending new data to adapter
                chatAdapter.notifyDataSetChanged(); //updating recyclerview/adapter with the new data
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

        //notificationService!!!!
//        vm.observeOnLatestChat().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(String senderUid) {
//                startService(senderUid);
//            }
//        });
    }
//    private void startService(String uid) {
//        Intent newChatNotificationIntent = new Intent(getContext(), NotificationsService.class);
//        newChatNotificationIntent.putExtra("ThisShouldBeAConstant", uid);
//        getActivity().startService(newChatNotificationIntent);
//    }
}