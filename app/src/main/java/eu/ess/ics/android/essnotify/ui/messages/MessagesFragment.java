package eu.ess.ics.android.essnotify.ui.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.databinding.FragmentMessagesBinding;

public class MessagesFragment extends Fragment implements MessageRefreshCompletionListener{

    private FragmentMessagesBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MessagesListAdapter messagesListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMessagesBinding.inflate(getLayoutInflater());

        messagesListAdapter = new MessagesListAdapter();
        binding.setMessagesListAdapter(messagesListAdapter);
        messagesListAdapter.addRefreshCompletionListener(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    messagesListAdapter.refresh();
                }
            });
        Toolbar toolbar = (Toolbar)view.findViewById(R.id.messageToolbar);
        toolbar.inflateMenu(R.menu.home_toolbar_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_delete_messages:
                    // Launch dialog to confirm deletion of all messages
                    return true;
                case R.id.action_filter_messages:
                    // Launch dialog to filter messages
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    public void messagesRefreshed(){
        swipeRefreshLayout.setRefreshing(false);
    }
}