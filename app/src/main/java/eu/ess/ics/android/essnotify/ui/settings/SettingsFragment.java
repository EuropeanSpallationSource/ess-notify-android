package eu.ess.ics.android.essnotify.ui.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.datamodel.UserService;

public class SettingsFragment extends Fragment {

    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private SettingsViewModel settingsViewModel;
    protected RecyclerView recyclerView;
    protected RecyclerView.LayoutManager layoutManager;

    protected SettingsListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        recyclerView = root.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(0);

        settingsViewModel.getServices(getActivity().getApplicationContext()).observe(getViewLifecycleOwner(), new Observer<List<UserService>>() {
            @Override
            public void onChanged(@Nullable List<UserService> services) {
                if(services != null) {
                    adapter = new SettingsListAdapter(services);
                    recyclerView.setAdapter(adapter);
                }
            }
        });

        // Listens for text input in the search input field.
        ((EditText)root.findViewById(R.id.search)).addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Let the adapter do the filtering.
                adapter.filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        return root;
    }
}