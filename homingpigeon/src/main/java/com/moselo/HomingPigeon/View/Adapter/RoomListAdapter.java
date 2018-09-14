package com.moselo.HomingPigeon.View.Adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.moselo.HomingPigeon.Helper.TimeFormatter;
import com.moselo.HomingPigeon.Helper.Utils;
import com.moselo.HomingPigeon.Listener.RoomListListener;
import com.moselo.HomingPigeon.Manager.DataManager;
import com.moselo.HomingPigeon.Model.MessageModel;
import com.moselo.HomingPigeon.Model.UserModel;
import com.moselo.HomingPigeon.R;
import com.moselo.HomingPigeon.View.Activity.ChatActivity;
import com.moselo.HomingPigeon.ViewModel.RoomListViewModel;

import java.util.List;

import static com.moselo.HomingPigeon.Helper.DefaultConstant.K_ROOM_ID;
import static com.moselo.HomingPigeon.Helper.DefaultConstant.K_USER;
import static com.moselo.HomingPigeon.View.Helper.Const.K_COLOR;
import static com.moselo.HomingPigeon.View.Helper.Const.K_MY_USERNAME;
import static com.moselo.HomingPigeon.View.Helper.Const.K_THEIR_USERNAME;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomListHolder> {

    //    private List<MessageModel> roomList;
    private RoomListViewModel vm;
    private RoomListListener roomListListener;
    private ColorStateList avatarTint;
    private String myUsername;

    public RoomListAdapter(/*List<MessageModel> roomList*/RoomListViewModel vm, String myUsername, RoomListListener roomListListener) {
        this.vm = vm;
        this.myUsername = myUsername;
        this.roomListListener = roomListListener;
    }

    @NonNull
    @Override
    public RoomListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_user_room, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RoomListHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        if (null != vm.getRoomList()) return vm.getRoomList().size();
        return 0;
    }

    public List<MessageModel> getItems() {
        return vm.getRoomList();
    }

    public MessageModel getItemAt(int position) {
        return vm.getRoomList().get(position);
    }

    class RoomListHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout clContainer;
        private ImageView ivAvatar, ivAvatarIcon, ivMute, ivMessageStatus;
        private TextView tvFullName, tvLastMessage, tvLastMessageTime, tvBadgeUnread;
        private MessageModel item;

        RoomListHolder(View itemView) {
            super(itemView);

            clContainer = itemView.findViewById(R.id.cl_container);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivAvatarIcon = itemView.findViewById(R.id.iv_avatar_icon);
            ivMute = itemView.findViewById(R.id.iv_mute);
            ivMessageStatus = itemView.findViewById(R.id.iv_message_status);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvLastMessageTime = itemView.findViewById(R.id.tv_last_message_time);
            tvBadgeUnread = itemView.findViewById(R.id.tv_badge_unread);
        }

        void onBind(int position) {
            item = getItemAt(position);
//            final UserModel userModel = Utils.getInstance().fromJSON(new TypeReference<UserModel>() {},item.getUser());
            final UserModel userModel = item.getUser();
            final int randomColor = Utils.getInstance().getRandomColor(userModel.getName());

            // TODO: 6 September 2018 LOAD AVATAR IMAGE TO VIEW
            avatarTint = ColorStateList.valueOf(randomColor);
//            tvAvatar.setText(userModel.getName().substring(0, 1).toUpperCase());
            ivAvatar.setBackgroundTintList(avatarTint);

            // Change avatar icon and background
            Resources resource = itemView.getContext().getResources();
            if (item.getRoom().isSelected()) {
                // Item is selected
                ivAvatarIcon.setImageDrawable(resource.getDrawable(R.drawable.ic_select));
                clContainer.setBackgroundColor(resource.getColor(R.color.transparent_grey));
            } else {
                // Item not selected
                // TODO: 7 September 2018 SET AVATAR ICON ACCORDING TO USER ROLE
                ivAvatarIcon.setImageDrawable(resource.getDrawable(R.drawable.ic_verified));
                clContainer.setBackgroundColor(resource.getColor(R.color.transparent));
            }

            // Set name, last message, and timestamp text
            tvFullName.setText(userModel.getName());
            tvLastMessage.setText(item.getMessage());
            tvLastMessageTime.setText(TimeFormatter.durationString(item.getCreated()));

            // Check if room is muted
            if (item.getRoom().isMuted()) {
                ivMute.setVisibility(View.VISIBLE);
                tvBadgeUnread.setBackground(resource.getDrawable(R.drawable.bg_9b9b9b_rounded_10dp));
            } else {
                ivMute.setVisibility(View.GONE);
                tvBadgeUnread.setBackground(resource.getDrawable(R.drawable.bg_amethyst_mediumpurple_270_rounded_10dp));
            }

            // TODO: 6 September 2018 CHANGE MESSAGE STATUS IMAGE

            // Show unread count
            int unreadCount = item.getRoom().getUnreadCount();
            if (0 < unreadCount && unreadCount < 100) {
                tvBadgeUnread.setText(item.getRoom().getUnreadCount() + "");
                ivMessageStatus.setVisibility(View.GONE);
                tvBadgeUnread.setVisibility(View.VISIBLE);
            } else if (unreadCount >= 100) {
                tvBadgeUnread.setText(R.string.over_99);
                ivMessageStatus.setVisibility(View.GONE);
                tvBadgeUnread.setVisibility(View.VISIBLE);
            } else {
                ivMessageStatus.setVisibility(View.VISIBLE);
                tvBadgeUnread.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (vm.isSelecting()) {
                    // Select room when other room is already selected
                    item.getRoom().setSelected(!item.getRoom().isSelected());
                    roomListListener.onRoomSelected(item, item.getRoom().isSelected());
                    notifyItemChanged(position);
                } else {
                    // Open chat room on click
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
                    UserModel myUser = Utils.getInstance().fromJSON(new TypeReference<UserModel>() {
                    }, prefs.getString(K_USER, ""));

                    String myUserID = myUser.getUserID();

                    if (!(myUserID+"-"+myUserID).equals(item.getRoom().getRoomID())) {
                        Intent intent = new Intent(itemView.getContext(), ChatActivity.class);
                        intent.putExtra(K_MY_USERNAME, myUsername);
                        intent.putExtra(K_THEIR_USERNAME, userModel.getName());
                        intent.putExtra(K_COLOR, randomColor);
                        intent.putExtra(K_ROOM_ID, item.getRoom().getRoomID());
                        itemView.getContext().startActivity(intent);

                        DataManager.getInstance().saveRecipientID(itemView.getContext(), item.getRecipientID());
                    }else {
                        Toast.makeText(itemView.getContext(), "Invalid Room", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Select room on long click
            itemView.setOnLongClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                item.getRoom().setSelected(!item.getRoom().isSelected());
                roomListListener.onRoomSelected(item, item.getRoom().isSelected());
                notifyItemChanged(position);
                return true;
            });
        }
    }
}