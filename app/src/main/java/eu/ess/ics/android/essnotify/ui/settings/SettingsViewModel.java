package eu.ess.ics.android.essnotify.ui.settings;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import eu.ess.ics.android.essnotify.BackendService;
import eu.ess.ics.android.essnotify.ServerAPIBase;
import eu.ess.ics.android.essnotify.datamodel.UserService;
import retrofit2.Call;
import retrofit2.Response;

public class SettingsViewModel extends ViewModel {

    private MutableLiveData<List<UserService>> userServices;

    public LiveData<List<UserService>> getServices(Context context) {
        if(userServices == null){
            userServices = new MutableLiveData<>();
            try {
                List<UserService> services = new GetServicesTask(context).execute().get();
                userServices.setValue(services);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userServices;
    }

    private class GetServicesTask extends AsyncTask<Void, Void, List<UserService>> {

        private Context context;

        public GetServicesTask(Context context){
            this.context = context;
        }

        @Override
        public List<UserService> doInBackground(Void... args) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(context);
            Call<List<UserService>> call = backendService.getUserServices();
            try {
                Response<List<UserService>> response = call.execute();
                return response.body();
            } catch (Exception e) {
                return null;
            }
        }
    }


}