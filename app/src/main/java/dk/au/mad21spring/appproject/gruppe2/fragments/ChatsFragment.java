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

import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.adapters.UserAdapter;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.ChatFragmentViewModel;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.UserFragmentsViewModel;


public class ChatsFragment extends Fragment {

    //UI
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;

    private List<User> mUsers;
    private List<String> userList;

    private ChatFragmentViewModel vm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setUpViewModel();

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getUserIdsFromCurrentUsersChats();

        return view;
    }

    private void setUpViewModel() {
        vm = new ViewModelProvider(this).get(ChatFragmentViewModel.class);
        vm.init(getActivity().getApplication());
    }

    private void getUserIdsFromCurrentUsersChats() {
        vm.getListOfUsersIdsFromMyChats().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                readChats();
            }
        });
    }

    private void readChats() {

    }


    // det ser ud som om jeg skal initiere userlist fra min fragment men ikke lytte på den.
    // Herefter skal jeg i stedte lytte på mUsers(af chats tror jeg nok)
    // Denne funktion skal opdatere en mUsers i repo på baggrund af den userlist der blevet genereret efter mit indledende kald.
}