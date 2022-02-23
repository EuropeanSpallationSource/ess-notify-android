package eu.ess.ics.android.essnotify.ui.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.databinding.FragmentMessagesBinding;
import eu.ess.ics.android.essnotify.pushmessaging.PushMessageService;

public class MessagesFragment extends Fragment implements MessageRefreshCompletionListener{

    private MessageDetailsViewModel messageDetailsViewModel;
    private FragmentMessagesBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MessagesListAdapter messagesListAdapter;
    /**
     * {@link BroadcastReceiver} handling broadcast when {@link eu.ess.ics.android.essnotify.pushmessaging.PushMessageService}
     * receives notification. The idea is to update the list of messages in response to notifications.
     */
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            messagesListAdapter.refresh(false);
        }
    };

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        getActivity().registerReceiver(myReceiver, new IntentFilter(PushMessageService.NEW_NOTIFICATION));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unregisterReceiver(myReceiver);
    }

    @Override
    public void onResume(){
        super.onResume();
        messagesListAdapter.refresh(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMessagesBinding.inflate(getLayoutInflater());

        messagesListAdapter = new MessagesListAdapter(messageDetailsData -> {
            messageDetailsViewModel.select(messageDetailsData);
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.messageDetailsDialog);
        });

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
                    messagesListAdapter.refresh(true);
                }
            });
        Toolbar toolbar = (Toolbar)view.findViewById(R.id.messageToolbar);
        toolbar.inflateMenu(R.menu.home_toolbar_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_delete_messages:
                    if(messagesListAdapter.getItemCount() > 0){
                        new AlertDialog.Builder(getContext())
                                .setTitle(getContext().getResources().getString(R.string.delete_all_messages))
                                .setIcon(R.drawable.ic_baseline_warning_amber_24)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        messagesListAdapter.deleteAllMessages();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                    return true;
                case R.id.action_filter_messages:
                    messagesListAdapter.showFilterDialog(getParentFragmentManager());
                    return true;
                case R.id.action_mark_all_as_read:
                    messagesListAdapter.markAllAsRead();
                    return true;
                default:
                    return false;
            }
        });

        messageDetailsViewModel =
                new ViewModelProvider(requireActivity()).get(MessageDetailsViewModel.class);
    }

    @Override
    public void messagesRefreshed(){
        swipeRefreshLayout.setRefreshing(false);
    }

    @FunctionalInterface
    public interface LaunchDetailView<Void, MessageDetailsData>{
        public void launch(eu.ess.ics.android.essnotify.ui.messages.MessageDetailsData messageDetailsData);
    }
}