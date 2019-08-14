package io.taptalk.TapTalk.View.Adapter

import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.DiffResult
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.taptalk.TapTalk.DiffCallback.TAPGroupMemberDiffCallback
import io.taptalk.TapTalk.Helper.CircleImageView
import io.taptalk.TapTalk.Helper.TAPBaseViewHolder
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Listener.TAPGroupMemberListListener
import io.taptalk.TapTalk.Manager.TAPChatManager
import io.taptalk.TapTalk.Model.TAPUserModel
import io.taptalk.Taptalk.R

class TAPGroupMemberAdapter(cellMode: Int, members: List<TAPUserModel>, adminList: List<String>, groupInterface: TAPGroupMemberListListener) : TAPBaseAdapter<TAPUserModel, TAPBaseViewHolder<TAPUserModel>>() {

    var adminList: MutableList<String> = mutableListOf()

    init {
        items = members
        this.adminList = adminList.toMutableList()
    }

    var cellMode = cellMode
    val groupInterface = groupInterface

    companion object {
        const val NORMAL_MODE = 1
        const val SELECT_MODE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TAPBaseViewHolder<TAPUserModel> {
        return MemberViewHolder(this, parent, R.layout.tap_cell_user_contact)
    }

    fun updateCellMode(cellMode: Int) {
        this.cellMode = cellMode
        notifyDataSetChanged()
    }

    fun setMemberItems(newList: List<TAPUserModel>) {
        val diffResult: DiffResult = DiffUtil.calculateDiff(TAPGroupMemberDiffCallback(items, newList), true)
        setItemsWithoutNotify(newList)
        diffResult.dispatchUpdatesTo(this@TAPGroupMemberAdapter)
    }

    class MemberViewHolder(adapter: TAPGroupMemberAdapter, parent: ViewGroup, itemLayoutId: Int) : TAPBaseViewHolder<TAPUserModel>(parent, itemLayoutId) {
        private val civAvatar: CircleImageView = itemView.findViewById(R.id.civ_avatar)
        private val tvAvatarLabel: TextView = itemView.findViewById(R.id.tv_avatar_label)
        private val tvFullName: TextView = itemView.findViewById(R.id.tv_full_name)
        private val tvMemberRole: TextView = itemView.findViewById(R.id.tv_member_role)
        private val vSeparator: View = itemView.findViewById(R.id.v_separator)
        private val ivSelection: ImageView = itemView.findViewById(R.id.iv_selection)
        private val groupAdapter = adapter
        private var isAdmin : Boolean = false;

        override fun onBind(item: TAPUserModel?, position: Int) {
            if (groupAdapter.adminList.isNotEmpty() && groupAdapter.adminList.contains(item?.userID
                            ?: "0")) {
                isAdmin = true
                tvMemberRole.visibility = View.VISIBLE
            } else {
                isAdmin = false
                tvMemberRole.visibility = View.GONE
            }

            // Load member avatar
            if (item?.avatarURL?.thumbnail.isNullOrEmpty()) {
                civAvatar.imageTintList = ColorStateList.valueOf(TAPUtils.getInstance().getRandomColor(item?.name))
                civAvatar.setImageResource(R.drawable.tap_bg_circle_9b9b9b)
                tvAvatarLabel.text = TAPUtils.getInstance().getInitials(item?.name, 2)
                tvAvatarLabel.visibility = View.VISIBLE
            } else {
                Glide.with(itemView.context)
                        .load(item?.avatarURL?.thumbnail)
                        .into(civAvatar)
                civAvatar.imageTintList = null
                tvAvatarLabel.visibility = View.GONE
            }

            // Set name
            tvFullName.text = item?.name ?: ""

            // Hide separator on last item
            if (position == groupAdapter.itemCount - 1) vSeparator.visibility = View.GONE
            else vSeparator.visibility = View.VISIBLE

            // Show or hide selection
            if (groupAdapter.cellMode == SELECT_MODE &&
                    item?.userID != TAPChatManager.getInstance().activeUser.userID) {
                ivSelection.visibility = View.VISIBLE
            } else {
                ivSelection.visibility = View.GONE
            }
            if (SELECT_MODE == groupAdapter.cellMode && true == item?.isSelected) {
                ivSelection.setImageResource(R.drawable.tap_ic_circle_active)
                ivSelection.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(TapTalk.appContext, R.color.tapIconCircleSelectionActive))
            } else {
                ivSelection.setImageResource(R.drawable.tap_ic_circle_inactive)
                ivSelection.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(TapTalk.appContext, R.color.tapIconCircleSelectionInactive))
            }

            // Set listener for click
            itemView.setOnClickListener {
                if (SELECT_MODE == groupAdapter.cellMode && false == item?.isSelected &&
                        item.userID != TAPChatManager.getInstance().activeUser.userID) {
                    groupAdapter.groupInterface.onContactSelected(item)
                    item.isSelected = true
                    ivSelection.setImageResource(R.drawable.tap_ic_circle_active)
                    ivSelection.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(TapTalk.appContext, R.color.tapIconCircleSelectionActive))
                } else if (SELECT_MODE == groupAdapter.cellMode && true == item?.isSelected &&
                        item.userID != TAPChatManager.getInstance().activeUser.userID) {
                    groupAdapter.groupInterface.onContactDeselected(item)
                    item.isSelected = false
                    ivSelection.setImageResource(R.drawable.tap_ic_circle_inactive)
                    ivSelection.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(TapTalk.appContext, R.color.tapIconCircleSelectionInactive))
                } else if (NORMAL_MODE == groupAdapter.cellMode) {
                    groupAdapter.groupInterface.onGroupMemberClicked(item, isAdmin)
                }
            }

            // Set listener for long press
            itemView.setOnLongClickListener {
                if (NORMAL_MODE == groupAdapter.cellMode && item?.userID != TAPChatManager.getInstance().activeUser.userID) {
                    item?.isSelected = true
                    groupAdapter.groupInterface.onContactLongPress(item)
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }

        }

    }

}