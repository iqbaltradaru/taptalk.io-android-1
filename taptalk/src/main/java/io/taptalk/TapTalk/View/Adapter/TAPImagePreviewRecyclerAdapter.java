package io.taptalk.TapTalk.View.Adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import io.taptalk.TapTalk.Helper.TAPBaseViewHolder;
import io.taptalk.TapTalk.Helper.TAPUtils;
import io.taptalk.TapTalk.Model.TAPImagePreviewModel;
import io.taptalk.TapTalk.View.Activity.TAPImagePreviewActivity;
import io.taptalk.Taptalk.R;

public class TAPImagePreviewRecyclerAdapter extends TAPBaseAdapter<TAPImagePreviewModel, TAPBaseViewHolder<TAPImagePreviewModel>> {

    TAPImagePreviewActivity.ImageThumbnailPreviewInterface thumbInterface;

    public TAPImagePreviewRecyclerAdapter(ArrayList<TAPImagePreviewModel> images, TAPImagePreviewActivity.ImageThumbnailPreviewInterface thumbInterface) {
        setItems(images);
        this.thumbInterface = thumbInterface;
    }

    @Override
    public TAPBaseViewHolder<TAPImagePreviewModel> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ImagePreviewVH(viewGroup, R.layout.tap_image_preview_recycler);
    }

    class ImagePreviewVH extends TAPBaseViewHolder<TAPImagePreviewModel> {
        ImageView ivImagePreview;
        FrameLayout flImagePreview;
        FrameLayout flDelete;

        protected ImagePreviewVH(ViewGroup parent, int itemLayoutId) {
            super(parent, itemLayoutId);
            ivImagePreview = itemView.findViewById(R.id.iv_image_preview);
            flImagePreview = itemView.findViewById(R.id.fl_image_preview);
            flDelete = itemView.findViewById(R.id.fl_delete);
        }

        @Override
        protected void onBind(TAPImagePreviewModel item, int position) {
            Glide.with(itemView.getContext()).load(item.getImageUris()).apply(new RequestOptions().centerCrop()).into(ivImagePreview);

            if (item.isSelected()) {
                flImagePreview.setBackground(itemView.getResources().getDrawable(R.drawable.tap_bg_transparent_stroke_greenblue_2dp));
                int marginSize = TAPUtils.getInstance().dpToPx(2);
                TAPUtils.getInstance().setMargins(ivImagePreview, marginSize, marginSize, marginSize, marginSize);
                flDelete.setVisibility(View.VISIBLE);
            } else {
                flImagePreview.setBackground(null);
                TAPUtils.getInstance().setMargins(ivImagePreview, 0, 0, 0, 0);
                flDelete.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> thumbInterface.onThumbnailTap(position, item));
        }
    }
}