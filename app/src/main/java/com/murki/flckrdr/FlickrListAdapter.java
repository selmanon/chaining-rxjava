package com.murki.flckrdr;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.murki.flckrdr.viewmodel.FlickrCardVM;
import java.util.List;

public class FlickrListAdapter extends RecyclerView.Adapter<FlickrListAdapter.BindingHolder> {

  private static final String CLASSNAME = FlickrListAdapter.class.getCanonicalName();

  private List<FlickrCardVM> dataSet;

  public FlickrListAdapter(List<FlickrCardVM> dataSet) {
    this.dataSet = dataSet;
  }

  @Override
  public BindingHolder onCreateViewHolder(ViewGroup parent, int type) {
    // create a new view
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.flickr_card, parent, false);

    return new BindingHolder(v);
  }

  @Override
  public void onBindViewHolder(BindingHolder bindingHolder, int i) {
    final FlickrCardVM itemVM = dataSet.get(i);
    bindingHolder.getBinding().setVariable(com.murki.flckrdr.BR.viewModel, itemVM);
    bindingHolder.getBinding().executePendingBindings();
  }

  @Override
  public int getItemCount() {
    if (dataSet == null) {
      return 0;
    } else {
      return dataSet.size();
    }
  }

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  public static class BindingHolder extends RecyclerView.ViewHolder {

    private ViewDataBinding binding;

    public BindingHolder(View itemView) {
      super(itemView);
      binding = DataBindingUtil.bind(itemView);
    }

    public ViewDataBinding getBinding() {
      return binding;
    }
  }
}
