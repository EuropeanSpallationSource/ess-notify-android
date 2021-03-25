package eu.ess.ics.android.essnotify.ui.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    protected SettingsListAdapter adapter;
    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding =
                FragmentSettingsBinding.inflate(getLayoutInflater());

        adapter = new SettingsListAdapter();
        binding.setServiceListAdapter(adapter);

        // Listens for text input in the search input field.
        ((EditText)binding.getRoot().findViewById(R.id.search)).addTextChangedListener(new TextWatcher() {
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

        return binding.getRoot();
    }
}