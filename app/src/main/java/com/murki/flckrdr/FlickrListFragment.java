package com.murki.flckrdr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.murki.flckrdr.repository.FlickrDomainService;
import com.murki.flckrdr.viewmodel.FlickrCardVM;
import java.util.List;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class FlickrListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

  private static final String CLASSNAME = FlickrListFragment.class.getCanonicalName();
  private FlickrDomainService flickrDomainService;
  private RecyclerView recyclerView;
  private SwipeRefreshLayout swipeRefreshLayout;
  private FlickrListAdapter flickrListAdapter;
  private Subscription flickrListSubscription;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(CLASSNAME, "onCreate()");
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Log.d(CLASSNAME, "onCreateView()");

    View view = inflater.inflate(R.layout.fragment_flickr_list, container, false);
    setupView(view);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Log.d(CLASSNAME, "onActivityCreated()");

    flickrDomainService = new FlickrDomainService(getContext()); // TODO: Inject Singleton
    fetchFlickrItems();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(CLASSNAME, "onDestroy()");

    unsubscribe();
  }

  @Override
  public void onRefresh() {
    Log.d(CLASSNAME, "onRefresh()");

    fetchFlickrItems();
  }

  private void setupView(View view) {
    swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.flickr_swipe_refresh);
    swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_dark);
    swipeRefreshLayout.setOnRefreshListener(this);

    recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    recyclerView.setHasFixedSize(true);
    // use a linear layout manager
    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
    // specify an adapter
    recyclerView.setAdapter(flickrListAdapter);
  }

  private void fetchFlickrItems() {
    isRefreshing(true);
    unsubscribe();
    Observable<List<FlickrCardVM>> recentPhotosObservable = flickrDomainService
        .getRecentPhotos()
        .observeOn(AndroidSchedulers.mainThread(), true); // delayError = true

    flickrListSubscription =
        recentPhotosObservable.subscribe(flickrRecentPhotosOnNext, flickrRecentPhotosOnError,
            flickrRecenPhotosOnComplete);
  }

  private void isRefreshing(final boolean isRefreshing) {
    swipeRefreshLayout.post(new Runnable() {
      @Override
      public void run() {
        swipeRefreshLayout.setRefreshing(isRefreshing);
      }
    });
  }

  private final Action1<List<FlickrCardVM>> flickrRecentPhotosOnNext =
      new Action1<List<FlickrCardVM>>() {
        @Override
        public void call(List<FlickrCardVM> flickrCardVMs) {
          Log.d(CLASSNAME, "flickrRecentPhotosOnNext.call() - Displaying card VMs in Adapter");
          // refresh the list adapter
          recyclerView.swapAdapter(flickrListAdapter = new FlickrListAdapter(flickrCardVMs), false);
        }
      };

  private final Action1<Throwable> flickrRecentPhotosOnError = new Action1<Throwable>() {
    @Override
    public void call(Throwable throwable) {
      Log.e(CLASSNAME, "flickrRecentPhotosOnError.call() - ERROR", throwable);
      isRefreshing(false);
      Toast.makeText(getActivity(), "OnError=" + throwable.getMessage(), Toast.LENGTH_LONG).show();
    }
  };

  private final Action0 flickrRecenPhotosOnComplete = new Action0() {
    @Override
    public void call() {
      Log.d(CLASSNAME, "flickrRecenPhotosOnComplete.call() - Data completed. Loading done.");
      isRefreshing(false);
    }
  };

  private void unsubscribe() {
    if (flickrListSubscription != null) {
      flickrListSubscription.unsubscribe();
      flickrListSubscription = null;
    }
  }
}
