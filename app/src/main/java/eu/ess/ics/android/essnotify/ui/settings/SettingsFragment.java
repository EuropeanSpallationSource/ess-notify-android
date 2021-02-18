package eu.ess.ics.android.essnotify.ui.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import java.util.List;

import eu.ess.ics.android.essnotify.BackendService;
import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.ServerAPIBase;
import eu.ess.ics.android.essnotify.databinding.FragmentSettingsBinding;
import eu.ess.ics.android.essnotify.datamodel.UserService;
import retrofit2.Call;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    protected SettingsListAdapter adapter;
    FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding =
                FragmentSettingsBinding.inflate(getLayoutInflater());

        adapter = new SettingsListAdapter();
        binding.setServiceListAdapter(adapter);

        getServicesList();

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

    private void getServicesList(){
        new HandleSubscriptionTask().execute();
    }


    private class HandleSubscriptionTask extends AsyncTask<Void, Void, List<UserService>> {

        @Override
        public List<UserService> doInBackground(Void... args) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(getActivity().getApplicationContext());
            Call<List<UserService>> call = backendService.getUserServices();
            try {
                Response<List<UserService>> response = call.execute();
                return response.body();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onPostExecute(List<UserService> userServiceList){
            if(userServiceList != null) {
                adapter.setServicesList(userServiceList);
            }
        }
    }
}