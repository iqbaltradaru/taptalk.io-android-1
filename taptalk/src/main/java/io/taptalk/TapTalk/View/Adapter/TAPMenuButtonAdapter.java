package io.taptalk.TapTalk.View.Adapter;

import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import io.taptalk.TapTalk.Helper.TAPBaseViewHolder;
import io.taptalk.TapTalk.Model.TAPMenuItem;
import io.taptalk.TapTalk.View.Activity.TAPChatProfileActivity;
import io.taptalk.Taptalk.R;

import static io.taptalk.TapTalk.Const.TAPDefaultConstant.DEFAULT_ANIMATION_TIME;

public class TAPMenuButtonAdapter extends TAPBaseAdapter<TAPMenuItem, TAPBaseViewHolder<TAPMenuItem>> {

    private TAPChatProfileActivity.ProfileMenuInterface menuInterface;

    public TAPMenuButtonAdapter(List<TAPMenuItem> items, TAPChatProfileActivity.ProfileMenuInterface menuInterface) {
        setItems(items, true);
        this.menuInterface = menuInterface;
    }

    @NonNull
    @Override
    public TAPBaseViewHolder<TAPMenuItem> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuButtonVH(parent, R.layout.tap_cell_menu_button);
    }

    private class MenuButtonVH extends TAPBaseViewHolder<TAPMenuItem> {

        private ConstraintLayout clContainer;
        private ImageView ivMenuIcon;
        private TextView tvMenuLabel;
        private Switch swMenuSwitch;

        MenuButtonVH(ViewGroup parent, int itemLayoutId) {
            super(parent, itemLayoutId);
            clContainer = itemView.findViewById(R.id.cl_container);
            ivMenuIcon = itemView.findViewById(R.id.iv_menu_icon);
            tvMenuLabel = itemView.findViewById(R.id.tv_menu_label);
            swMenuSwitch = itemView.findViewById(R.id.sw_menu_switch);
        }

        @Override
        protected void onBind(TAPMenuItem item, int position) {
            ivMenuIcon.setImageResource(item.getIconRes());
            tvMenuLabel.setText(item.getMenuLabel());
            tvMenuLabel.setTextColor(ContextCompat.getColor(itemView.getContext(), item.getTextColorRes()));

            if (item.isSwitchMenu()) {
                swMenuSwitch.setVisibility(View.VISIBLE);
                swMenuSwitch.setChecked(item.isChecked());
                swMenuSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    private ValueAnimator transitionToOrange, transitionToGrey;

                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        item.setChecked(isChecked);
                        menuInterface.onMenuClicked(item);
                        if (isChecked) {
                            // Turn switch ON
                            getTransitionGrey().cancel();
                            getTransitionOrange().start();
                        } else {
                            // Turn switch OFF
                            getTransitionOrange().cancel();
                            getTransitionGrey().start();
                        }
                    }

                    private ValueAnimator getTransitionOrange() {
                        if (null == transitionToOrange) {
                            transitionToOrange = ValueAnimator.ofArgb(
                                    itemView.getContext().getResources().getColor(R.color.tap_grey_9b),
                                    itemView.getContext().getResources().getColor(R.color.tap_pumkin_orange_two));
                            transitionToOrange.setDuration(DEFAULT_ANIMATION_TIME);
                            transitionToOrange.addUpdateListener(valueAnimator -> ivMenuIcon.setColorFilter(
                                    (Integer) valueAnimator.getAnimatedValue(), PorterDuff.Mode.SRC_IN));
                        }
                        return transitionToOrange;
                    }

                    private ValueAnimator getTransitionGrey() {
                        if (null == transitionToGrey) {
                            transitionToGrey = ValueAnimator.ofArgb(
                                    itemView.getContext().getResources().getColor(R.color.tap_pumkin_orange_two),
                                    itemView.getContext().getResources().getColor(R.color.tap_grey_9b));
                            transitionToGrey.setDuration(DEFAULT_ANIMATION_TIME);
                            transitionToGrey.addUpdateListener(valueAnimator -> ivMenuIcon.setColorFilter(
                                    (Integer) valueAnimator.getAnimatedValue(), PorterDuff.Mode.SRC_IN));
                        }
                        return transitionToGrey;
                    }
                });
            } else {
                swMenuSwitch.setVisibility(View.GONE);
            }

            clContainer.setOnClickListener(v -> {
                if (item.isSwitchMenu()) {
                    swMenuSwitch.setChecked(!swMenuSwitch.isChecked());
                } else {
                    menuInterface.onMenuClicked(item);
                }
            });
        }
    }
}