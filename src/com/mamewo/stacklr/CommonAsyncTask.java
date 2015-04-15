package com.mamewo.stacklr;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import android.os.AsyncTask;
import android.view.View;
import static com.mamewo.stacklr.Constant.*;

import java.io.IOException;

abstract class CommonAsyncTask
	extends AsyncTask<Void, Void, Boolean>
{
	final StacklrExpActivity activity_;
	final com.google.api.services.tasks.Tasks client_;
	//	private final View progressBar;

	CommonAsyncTask(StacklrExpActivity activity) {
		activity_ = activity;
		client_ = activity.service_;
		//progressBar = activity.findViewById(R.id.title_refresh_progress);
	}

	@Override
		protected void onPreExecute() {
		super.onPreExecute();
		activity_.numAsyncTasks++;
		//progressBar.setVisibility(View.VISIBLE);
	}

	@Override
		protected final Boolean doInBackground(Void... ignored) {
		try {
			doInBackground();
			return true;
		}
		catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
			activity_.showGooglePlayServicesAvailabilityErrorDialog(
																   availabilityException.getConnectionStatusCode());
		}
		catch (UserRecoverableAuthIOException userRecoverableException) {
			activity_.startActivityForResult(
											 userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
		}
		catch (IOException e) {
			Utils.logAndShow(activity_, TAG, e);
		}
		return false;
	}

	@Override
		protected final void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		--activity_.numAsyncTasks;
		// if (0 == --activity.numAsyncTasks) {
		// 	progressBar.setVisibility(View.GONE);
		// }
		if (success) {
			activity_.refreshView();
		}
	}

	abstract protected void doInBackground() throws IOException;
}
