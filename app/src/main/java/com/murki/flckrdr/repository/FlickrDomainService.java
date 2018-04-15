package com.murki.flckrdr.repository;

import android.content.Context;
import android.util.Log;
import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.murki.flckrdr.model.RecentPhotosResponse;
import com.murki.flckrdr.viewmodel.FlickrCardVM;
import com.murki.flckrdr.viewmodel.FlickrModelToVmMapping;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FlickrDomainService {

  private static final String CLASSNAME = FlickrDomainService.class.getCanonicalName();

  private final FlickrNetworkRepository flickrNetworkRepository;
  private final FlickrDiskRepository flickrDiskRepository;

  public FlickrDomainService(Context context) {
    flickrNetworkRepository = new FlickrNetworkRepository(); // TODO: Inject Singleton
    flickrDiskRepository = new FlickrDiskRepository(context); // TODO: Inject Singleton
  }

  @RxLogObservable
  public Observable<List<FlickrCardVM>> getRecentPhotos() {
    return getMergedPhotos()
        .filter(new Func1<RecentPhotosResponse, Boolean>() {
          @Override public Boolean call(RecentPhotosResponse recentPhotosResponse) {
            return recentPhotosResponse != null;
          }
        })
        .map(FlickrModelToVmMapping.instance());
  }

  @RxLogObservable
  private Observable<RecentPhotosResponse> getMergedPhotos() {
    return Observable.merge(
        flickrDiskRepository.getRecentPhotos().subscribeOn(Schedulers.io()),
        flickrNetworkRepository.getRecentPhotos().doOnNext(new Action1<RecentPhotosResponse>() {
          @Override
          public void call(RecentPhotosResponse recentPhotosResponse) {
            Log.d(CLASSNAME,
                "flickrApiRepository.getRecentPhotos().doOnNext() - Saving photos to disk - thread="
                    + Thread.currentThread().getName());
            flickrDiskRepository.savePhotos(recentPhotosResponse);
          }
        }).subscribeOn(Schedulers.io())
    );
  }
}
