package cpp_skywell.androcal.Service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cpp_skywell.androcal.ContentProvider.EventsDO;
import cpp_skywell.androcal.ContentProvider.Google.GEventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAOFactory;

/**
 * Created by zhangliang on 5/8/17.
 *
 * @deprecated use GoogleSyncAdapter
 */

public class GoogleSyncService extends JobService {
    public static final int JOB_ID = 8001;

    private UpdateAsyncTask syncTask = new UpdateAsyncTask();
    private EventsDAO mEventsDAO = null;

    public static boolean register(Context context) {
        ComponentName serviceName = new ComponentName(context, GoogleSyncService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("GoogleSyncService", "Job scheduled successfully!");
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean onStartJob(JobParameters params) {
        syncTask.execute(params);
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        mEventsDAO = EventsDAOFactory.create(this.getApplicationContext());
        mEventsDAO = new EventsDAO(this.getContentResolver());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class UpdateAsyncTask extends AsyncTask<JobParameters, Void, JobParameters[]> {

        @Override
        protected JobParameters[] doInBackground(JobParameters... params) {
            // Trigger Google authentication
//            try {
//                GEventsDAO.getInstance().checkAuth();
//            } catch(UserRecoverableAuthIOException e) {
//                startActivityForResult(
//                        e.getIntent(),
//                        REQUEST_AUTHORIZATION);
//
//            } catch (Exception e) {
//                Log.d("TestCaller", "", e);
//            }


            try {
                // Make sure upload data first
                this.syncLocal();
                this.syncRemote();
            } catch (Exception e) {
                Log.d("UpdateAsyncTask", "", e);
            }
            return params;
        }

        @Override
        protected void onPostExecute(JobParameters[] result) {
            for (JobParameters params : result) {
                jobFinished(params, false);
            }
        }

        private void syncLocal() throws IOException {
            GEventsDAO gdao = GEventsDAO.getInstance();

            // Get all dirties
            List<EventsDO> dirties = mEventsDAO.getByDirty(true);

            // Group dirty events
            List<EventsDO> newEvents = new ArrayList<EventsDO>();
            List<EventsDO> updateEvents = new ArrayList<EventsDO>();
            List<EventsDO> deleteEvents = new ArrayList<EventsDO>();
            List<EventsDO> localDeleteEvents = new ArrayList<EventsDO>();
            Iterator<EventsDO> it = dirties.iterator();
            while (it.hasNext()) {
                EventsDO event = it.next();
                if (event.getSource() == EventsDO.Source.NONE) { // Not exist on Google
                    if (event.getStatus() == EventsDO.STATUS_CANCEL) { // Deteleted locally
                        localDeleteEvents.add(event); // Not need to update Google, just delete it locally
                    } else { // Created locally, need to add on Google
                        newEvents.add(event);
                    }
                } else { // Exists on Google
                    if (event.getStatus() == EventsDO.STATUS_CANCEL) { // Deleted locally, need to delete on Google
                        deleteEvents.add(event);
                    } else { // Updated locally, need to update Google
                        updateEvents.add(event);
                    }
                }
            }

            Iterator<EventsDO> itReturn = null;

            // Delete ones not exist on Google
            it = localDeleteEvents.iterator();
            while (it.hasNext()) {
                EventsDO event = it.next();
                mEventsDAO.deleteById(event.getId());
                Log.d("syncLocal.ldel", event.toString());
            }

            // Batch sync new ones
            itReturn = gdao.addBatch(newEvents).iterator();
            while (itReturn.hasNext()) {
                EventsDO newEvent = itReturn.next();
                newEvent.setDirty(false);
                mEventsDAO.updateById(newEvent);
                Log.d("syncLocal.new", newEvent.toString());
            }

            // Batch sync updated ones
            itReturn = gdao.updateBatch(updateEvents).iterator();
            while (itReturn.hasNext()) {
                EventsDO newEvent = itReturn.next();
                newEvent.setDirty(false);
                mEventsDAO.updateById(newEvent);
                Log.d("syncLocal.update", newEvent.toString());
            }

            // Batch sync deleted ones
            gdao.deleteBatch(deleteEvents);
            it = deleteEvents.iterator();
            while (it.hasNext()) {
                Log.d("syncLocal.del", it.next().toString());
            }
        }

        private void syncRemote() throws IOException {
            GEventsDAO gdao = GEventsDAO.getInstance();

            try {
                // Get updated events from Google
                List<EventsDO> eventList = gdao.getUpdatedList(50);
                Iterator<EventsDO> it = eventList.iterator();

                // Update local database
                while (it.hasNext()) {
                    EventsDO event = it.next();
                    Log.d("syncRemote.new", event.toString());
                    if (event.getStatus() == EventsDO.STATUS_CANCEL) { // Events deleted on Google
                        mEventsDAO.deleteByRefId(event.getSource(), event.getRefId());
                    } else { // Events modified/added on Google
                        if (mEventsDAO.updateByRefId(event) == 0) {
                            mEventsDAO.add(event);
                        }
                    }
                }

                // Check sync result
                eventList = mEventsDAO.getByDateRange(null, null);
                it = eventList.iterator();
                while (it.hasNext()) {
                    Log.d("syncRemote.all", it.next().toString());
                }
            } catch (GEventsDAO.InvalidSyncTokenException e) {
                // TODO: handle sync token expired case
                Log.d("syncRemote", "Google sync token expired");
            }
        }
    }
}
