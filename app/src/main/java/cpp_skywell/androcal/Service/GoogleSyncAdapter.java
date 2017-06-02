package cpp_skywell.androcal.Service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
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
 * Created by zhangliang on 5/21/17.
 */

public class GoogleSyncAdapter extends AbstractThreadedSyncAdapter {
    EventsDAO mEventsDAO = null;

    public GoogleSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
//        mEventsDAO = EventsDAOFactory.create(context);
        mEventsDAO = new EventsDAO(context.getContentResolver());
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            // Make sure upload data first
            upload();
            download();
        } catch (Exception e) {
            Log.d("GoogleSyncAdapter", "", e);
        }

    }

    protected void upload() throws IOException {
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
            Log.d("syncUpload.ldel", event.toString());
        }

        // Batch sync new ones
        itReturn = gdao.addBatch(newEvents).iterator();
        while (itReturn.hasNext()) {
            EventsDO newEvent = itReturn.next();
            newEvent.setDirty(false);
            mEventsDAO.updateById(newEvent);
            Log.d("syncUpload.new", newEvent.toString());
        }

        // Batch sync updated ones
        itReturn = gdao.updateBatch(updateEvents).iterator();
        while (itReturn.hasNext()) {
            EventsDO newEvent = itReturn.next();
            newEvent.setDirty(false);
            mEventsDAO.updateById(newEvent);
            Log.d("syncUpload.update", newEvent.toString());
        }

        // Batch sync deleted ones
        gdao.deleteBatch(deleteEvents);
        it = deleteEvents.iterator();
        while (it.hasNext()) {
            Log.d("syncUpload.del", it.next().toString());
        }
    }

    protected void download() throws IOException {
        GEventsDAO gdao = GEventsDAO.getInstance();

        try {
            // Get updated events from Google
            List<EventsDO> eventList = gdao.getUpdatedList(50);
            Iterator<EventsDO> it = eventList.iterator();

            // Update local database
            while (it.hasNext()) {
                EventsDO event = it.next();
                Log.d("syncDownload.new", event.toString());
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
                Log.d("syncDownload.all", it.next().toString());
            }
        } catch (GEventsDAO.InvalidSyncTokenException e) {
            // TODO: handle sync token expired case
            Log.d("syncDownload", "Google sync token expired");
        }
    }
}
