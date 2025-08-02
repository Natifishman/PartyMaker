package com.example.partymaker.ui.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.partymaker.R;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.base.BaseAdapter;
import com.example.partymaker.utils.media.GlideImageLoader;
import com.example.partymaker.utils.business.sharing.ContentSharingManager;

/**
 * Adapter for displaying groups in a RecyclerView. Uses the BaseAdapter for
 * efficient updates and standardized functionality.
 */
public class GroupAdapter extends BaseAdapter<Group, GroupAdapter.GroupViewHolder> {

  private final Context context;
  private final OnGroupClickListener listener;

  /**
   * Constructs a GroupAdapter.
   *
   * @param context The context
   * @param listener The click listener
   */
  public GroupAdapter(Context context, OnGroupClickListener listener) {
    this.context = context;
    this.listener = listener;
    
    // Set up BaseAdapter click listener
    setOnItemClickListener((group, position) -> {
      if (listener != null) {
        listener.onGroupClick(group);
      }
    });
    
    // Set up long click for sharing
    setOnItemLongClickListener((group, position) -> {
      ContentSharingManager.sharePartyText(context, group);
      return true;
    });
  }

  @Override
  protected int getItemLayoutId() {
    return R.layout.item_group;
  }

  @Override
  @NonNull
  protected GroupViewHolder createViewHolder(@NonNull View view) {
    return new GroupViewHolder(view);
  }

  /** Interface for handling group click events. */
  public interface OnGroupClickListener {
    void onGroupClick(Group group);
  }
  
  /**
   * Updates the adapter with new items using efficient DiffUtil
   * @param newItems The new list of groups
   */
  public void updateItems(java.util.List<Group> newItems) {
    if (newItems != null) {
      clear();
      addItems(newItems);
    }
  }

  /** ViewHolder for group items. */
  static class GroupViewHolder extends BaseViewHolder {
    private final TextView groupNameTextView;
    private final TextView groupDateTextView;
    private final ImageView groupImageView;

    GroupViewHolder(@NonNull View itemView) {
      super(itemView);
      groupNameTextView = itemView.findViewById(R.id.tvGroupName);
      groupDateTextView = itemView.findViewById(R.id.tvGroupDate);
      groupImageView = itemView.findViewById(R.id.imgGroupPicture);
    }

    @Override
    public void bind(@NonNull Object item, int position) {
      if (!(item instanceof Group)) return;
      
      Group group = (Group) item;
      
      // Set group name
      if (group.getGroupName() != null) {
        groupNameTextView.setText(group.getGroupName());
      } else {
        groupNameTextView.setText("Unnamed Group");
      }

      // Set formatted date
      String formattedDate = formatGroupDate(group);
      groupDateTextView.setText(formattedDate);

      // Load group image
      loadGroupImage(group);
    }

    private String formatGroupDate(Group group) {
      try {
        return String.format("%s/%s/%s at %s", 
            group.getGroupDays(), 
            group.getGroupMonths(), 
            group.getGroupYears(), 
            group.getGroupHours());
      } catch (Exception e) {
        return "Date unavailable";
      }
    }
    
    private void loadGroupImage(Group group) {
      // Set default image (Group model doesn't have getGroupImageUrl method yet)
      groupImageView.setImageResource(R.drawable.default_group_image);
      
      // TODO: Add group image URL support when Group model is updated
      // For now, just use default image for all groups
    }
  }
}