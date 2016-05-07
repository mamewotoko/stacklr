package com.mamewo.stacklr;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import android.os.AsyncTask;
import android.view.View;
import android.util.Log;

import static com.mamewo.stacklr.Constant.*;

import java.io.IOException;

abstract class CommonAsyncTask
	extends AsyncTask<Void, Void, Boolean>
{
	final StacklrExpActivity activity_;
	final com.google.api.services.tasks.Tasks client_;
	//XXX
	final com.google.api.services.calendar.Calendar calendarClient_;

	//	private final View progressBar;
	public CommonAsyncTask(StacklrExpActivity activity) {
		activity_ = activity;
		client_ = activity.service_;
		calendarClient_ = activity.calendarService_;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(TAG, "onPreExecute: show progress " + this.getClass().getName());
		//TODO: synchronize?
		activity_.numAsyncTasks++;
		activity_.showLoadingIcon();
	}

	@Override
	protected final Boolean doInBackground(Void... ignored) {
		try {
			doInBackground();
			return true;
		}
		catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
			activity_.showGooglePlayServicesAvailabilityErrorDialog(availabilityException.getConnectionStatusCode());
		}
		catch (UserRecoverableAuthIOException userRecoverableException) {
			activity_.startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
		}
		catch (IOException e) {
			Utils.logAndShow(activity_, TAG, e);
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);

		//TODO: synchronize?
		if (0 == --activity_.numAsyncTasks) {
			activity_.hideLoadingIcon();
		}
		Log.d(TAG, "onPostExecute: " + this.getClass().getName());
	}

	abstract protected void doInBackground() throws IOException;
}
