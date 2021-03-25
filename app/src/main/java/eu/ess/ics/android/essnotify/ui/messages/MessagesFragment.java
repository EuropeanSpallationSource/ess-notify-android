package eu.ess.ics.android.essnotify.ui.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.databinding.FragmentMessagesBinding;

public class MessagesFragment extends Fragment {

    private FragmentMessagesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMessagesBinding.inflate(getLayoutInflater());
        binding.setMessagesListAdapter(new MessagesListAdapter());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
}