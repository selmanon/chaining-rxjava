package com.murki.flckrdr.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.murki.flckrdr.model.RecentPhotosResponse;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.concurrent.Callable;
import rx.Observable;

public class FlickrDiskRepository {

  private static final String CLASSNAME = FlickrDiskRepository.class.getCanonicalName();
  private final static String RECENT_PHOTOS_RESPONSE_KEY = CLASSNAME + ".RecentPhotosResponseKey";

  private final SharedPreferences sharedPreferences;
  private final JsonAdapter<RecentPhotosResponse> flickrPhotosJsonAdapter;

  public FlickrDiskRepository(Context context) {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    Moshi moshi = new Moshi.Builder().build();
    //Type adapterType = Types.newParameterizedType(Timestamped.class, RecentPhotosResponse.class);
    flickrPhotosJsonAdapter = moshi.adapter(RecentPhotosResponse.class);
  }

  public void savePhotos(RecentPhotosResponse photos) {
    String serializedPhotoList = flickrPhotosJsonAdapter.toJson(photos);
    sharedPreferences.edit().putString(RECENT_PHOTOS_RESPONSE_KEY, serializedPhotoList).apply();
  }

  @RxLogObservable
  public Observable<RecentPhotosResponse> getRecentPhotos() {
    return Observable.fromCallable(new Callable<RecentPhotosResponse>() {
      @Override
      public RecentPhotosResponse call() throws Exception {
        //                if (true) throw new RuntimeException("DISK.getRecentPhotos() fake Exception!");
        String serializedPhotoList = sharedPreferences.getString(RECENT_PHOTOS_RESPONSE_KEY, "");
        RecentPhotosResponse photos = null;
        if (!TextUtils.isEmpty(serializedPhotoList)) {
          photos = flickrPhotosJsonAdapter.fromJson(serializedPhotoList);
        }
        return photos;
      }
    });
  }
}