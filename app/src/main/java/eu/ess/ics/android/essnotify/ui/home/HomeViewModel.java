package eu.ess.ics.android.essnotify.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import eu.ess.ics.android.essnotify.R;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("No notifications");
    }

    public LiveData<String> getText() {
        return mText;
    }
}