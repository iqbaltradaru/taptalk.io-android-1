package io.taptalk.TapTalk.View.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.taptalk.TapTalk.API.View.TAPDefaultDataView;
import io.taptalk.TapTalk.Helper.OverScrolled.OverScrollDecoratorHelper;
import io.taptalk.TapTalk.Helper.TAPHorizontalDecoration;
import io.taptalk.TapTalk.Helper.TAPUtils;
import io.taptalk.TapTalk.Helper.TapTalkDialog;
import io.taptalk.TapTalk.Interface.TapTalkContactListInterface;
import io.taptalk.TapTalk.Manager.TAPDataManager;
import io.taptalk.TapTalk.Manager.TAPGroupManager;
import io.taptalk.TapTalk.Model.ResponseModel.TAPCreateRoomResponse;
import io.taptalk.TapTalk.Model.TAPErrorModel;
import io.taptalk.TapTalk.Model.TAPUserModel;
import io.taptalk.TapTalk.View.Adapter.TAPContactInitialAdapter;
import io.taptalk.TapTalk.View.Adapter.TAPContactListAdapter;
import io.taptalk.TapTalk.ViewModel.TAPContactListViewModel;
import io.taptalk.Taptalk.R;

import static io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.GROUP_MEMBERS;
import static io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.ROOM_ID;
import static io.taptalk.TapTalk.Const.TAPDefaultConstant.SHORT_ANIMATION_TIME;

public class TAPAddMembersActivity extends TAPBaseActivity {

    private ConstraintLayout clActionBar;
    private LinearLayout llGroupMembers;
    private ImageView ivButtonBack, ivButtonSearch, ivButtonClearText, ivLoadingProgressAddMembers;
    private TextView tvTitle, tvMemberCount, tvButtonAddMembers;
    private EditText etSearch;
    private RecyclerView rvContactList, rvGroupMembers;

    private TAPContactInitialAdapter contactListAdapter;
    private TAPContactListAdapter selectedMembersAdapter;
    private TapTalkContactListInterface listener;
    private TAPContactListViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tap_activity_add_members);
        ArrayList<TAPUserModel> existingMembers;

        //dapetin existing members dari group
        existingMembers = null == getIntent().getParcelableArrayListExtra(GROUP_MEMBERS) ?
                new ArrayList<>() : getIntent().getParcelableArrayListExtra(GROUP_MEMBERS);

        initViewModel(existingMembers);
        initListener();
        initView();
    }

    @Override
    public void onBackPressed() {
        if (vm.isSelecting()) {
            showToolbar();
        } else {
            super.onBackPressed();
            setResult(RESULT_CANCELED);
            overridePendingTransition(R.anim.tap_stay, R.anim.tap_slide_right);
        }
    }

    private void initViewModel(List<TAPUserModel> existingMembers) {
        vm = ViewModelProviders.of(this).get(TAPContactListViewModel.class);
        vm.setGroupSize(existingMembers.size());

        // Show users from contact list
        vm.getContactListLive().observe(this, userModels -> {
            vm.getContactList().clear();
            addFilteredContactList(userModels, existingMembers);
            vm.setSeparatedContacts(TAPUtils.getInstance().separateContactsByInitial(vm.getContactList()));
            runOnUiThread(() -> contactListAdapter.setItems(vm.getSeparatedContacts()));
        });
        vm.getFilteredContacts().addAll(vm.getContactList());
    }

    private void addFilteredContactList(List<TAPUserModel> userModels, List<TAPUserModel> existingMembers) {
        for (TAPUserModel contact : userModels) {
            if (!existingMembers.contains(contact)) {
                vm.getContactList().add(contact);
            }
        }
    }

    private void initListener() {
        listener = new TapTalkContactListInterface() {
            @Override
            public boolean onContactSelected(TAPUserModel contact) {
                TAPUtils.getInstance().dismissKeyboard(TAPAddMembersActivity.this);
                new Handler().post(waitAnimationsToFinishRunnable);
                if (!vm.getSelectedContacts().contains(contact)) {
                    if (vm.getSelectedContacts().size() + vm.getGroupSize() >= TAPGroupManager.Companion.getGetInstance().getGroupMaxParticipants()) {
                        // TODO: 20 September 2018 CHANGE DIALOG LISTENER
                        new TapTalkDialog.Builder(TAPAddMembersActivity.this)
                                .setDialogType(TapTalkDialog.DialogType.ERROR_DIALOG)
                                .setTitle(getString(R.string.tap_cannot_add_more_people))
                                .setMessage(getString(R.string.tap_group_limit_reached))
                                .setPrimaryButtonTitle(getString(R.string.tap_ok))
                                .setPrimaryButtonListener(v -> {

                                })
                                .show();
                        return false;
                    }
                    vm.addSelectedContact(contact);
                    selectedMembersAdapter.notifyItemInserted(vm.getSelectedContacts().size());
                    rvGroupMembers.scrollToPosition(vm.getSelectedContacts().size() - 1);
                    updateSelectedMemberDecoration();
                } else {
                    int index = vm.getSelectedContacts().indexOf(contact);
                    vm.removeSelectedContact(contact);
                    selectedMembersAdapter.notifyItemRemoved(index);
                }
                if (vm.getSelectedContacts().size() > 0) {
                    llGroupMembers.setVisibility(View.VISIBLE);
                } else {
                    llGroupMembers.setVisibility(View.GONE);
                }
                tvMemberCount.setText(String.format(getString(R.string.tap_selected_member_count), vm.getGroupSize() + vm.getSelectedContacts().size(), TAPGroupManager.Companion.getGetInstance().getGroupMaxParticipants()));
                return true;
            }

            @Override
            public void onContactDeselected(TAPUserModel contact) {
                TAPUtils.getInstance().dismissKeyboard(TAPAddMembersActivity.this);
                selectedMembersAdapter.removeItem(contact);
                new Handler().post(waitAnimationsToFinishRunnable);
                contactListAdapter.notifyDataSetChanged();
                if (vm.getSelectedContacts().size() > 0) {
                    llGroupMembers.setVisibility(View.VISIBLE);
                } else {
                    llGroupMembers.setVisibility(View.GONE);
                }
                tvMemberCount.setText(String.format(getString(R.string.tap_selected_member_count), vm.getGroupSize() + vm.getSelectedContacts().size(), TAPGroupManager.Companion.getGetInstance().getGroupMaxParticipants()));
            }
        };
    }

    private void initView() {
        clActionBar = findViewById(R.id.cl_action_bar);
        llGroupMembers = findViewById(R.id.ll_group_members);
        ivButtonBack = findViewById(R.id.iv_button_back);
        ivButtonSearch = findViewById(R.id.iv_button_search);
        ivButtonClearText = findViewById(R.id.iv_button_clear_text);
        ivLoadingProgressAddMembers = findViewById(R.id.iv_loading_progress_add_members);
        tvTitle = findViewById(R.id.tv_title);
        tvMemberCount = findViewById(R.id.tv_member_count);
        tvButtonAddMembers = findViewById(R.id.tv_add_members_btn);
        etSearch = findViewById(R.id.et_search);
        rvContactList = findViewById(R.id.rv_contact_list);
        rvGroupMembers = findViewById(R.id.rv_group_members);

        getWindow().setBackgroundDrawable(null);
        vm.setSeparatedContacts(TAPUtils.getInstance().separateContactsByInitial(vm.getFilteredContacts()));
        vm.setRoomID(null == getIntent().getStringExtra(ROOM_ID) ? "" : getIntent().getStringExtra(ROOM_ID));

        // All contacts adapter
        contactListAdapter = new TAPContactInitialAdapter(TAPContactListAdapter.SELECT, vm.getSeparatedContacts(), vm.getSelectedContacts(), listener);
        rvContactList.setAdapter(contactListAdapter);
        rvContactList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvContactList.setHasFixedSize(false);
        OverScrollDecoratorHelper.setUpOverScroll(rvContactList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        // Selected members adapter
        selectedMembersAdapter = new TAPContactListAdapter(TAPContactListAdapter.SELECTED_MEMBER, vm.getSelectedContacts(), listener);
        rvGroupMembers.setAdapter(selectedMembersAdapter);
        rvGroupMembers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        OverScrollDecoratorHelper.setUpOverScroll(rvGroupMembers, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
        new Handler().post(waitAnimationsToFinishRunnable);

        etSearch.addTextChangedListener(searchTextWatcher);
        etSearch.setOnEditorActionListener(searchEditorListener);

        ivButtonBack.setOnClickListener(v -> onBackPressed());
        ivButtonSearch.setOnClickListener(v -> showSearchBar());
        ivButtonClearText.setOnClickListener(v -> etSearch.setText(""));
        tvButtonAddMembers.setOnClickListener(v -> startAddMemberProcess());

        rvContactList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                TAPUtils.getInstance().dismissKeyboard(TAPAddMembersActivity.this);
            }
        });
    }

    private void showToolbar() {
        vm.setSelecting(false);
        TAPUtils.getInstance().dismissKeyboard(this);
        ivButtonBack.setImageResource(R.drawable.tap_ic_close_grey);
        tvTitle.setVisibility(View.VISIBLE);
        etSearch.setVisibility(View.GONE);
        etSearch.setText("");
        ivButtonSearch.setVisibility(View.VISIBLE);
        ((TransitionDrawable) clActionBar.getBackground()).reverseTransition(SHORT_ANIMATION_TIME);
    }

    private void showSearchBar() {
        vm.setSelecting(true);
        ivButtonBack.setImageResource(R.drawable.tap_ic_chevron_left_white);
        tvTitle.setVisibility(View.GONE);
        etSearch.setVisibility(View.VISIBLE);
        ivButtonSearch.setVisibility(View.GONE);
        TAPUtils.getInstance().showKeyboard(this, etSearch);
        ((TransitionDrawable) clActionBar.getBackground()).startTransition(SHORT_ANIMATION_TIME);
    }

    private void startAddMemberProcess() {
        if (!"".equals(vm.getRoomID())) {
            TAPDataManager.getInstance().addRoomParticipant(vm.getRoomID(), vm.getSelectedContactsIds(), addMemberView);
        }
    }

    private void updateSelectedMemberDecoration() {
        if (rvGroupMembers.getItemDecorationCount() > 0) {
            rvGroupMembers.removeItemDecorationAt(0);
        }
        rvGroupMembers.addItemDecoration(new TAPHorizontalDecoration(0, 0,
                0, TAPUtils.getInstance().dpToPx(16), selectedMembersAdapter.getItemCount(),
                0, 0));
    }

    private void updateFilteredContacts(String searchKeyword) {
        vm.getSeparatedContacts().clear();
        vm.getFilteredContacts().clear();
        if (searchKeyword.trim().isEmpty()) {
            vm.getFilteredContacts().addAll(vm.getContactList());
        } else {
            List<TAPUserModel> filteredContacts = new ArrayList<>();
            for (TAPUserModel user : vm.getContactList()) {
                if (user.getName().toLowerCase().contains(searchKeyword)) {
                    filteredContacts.add(user);
                }
            }
            vm.getFilteredContacts().addAll(filteredContacts);
        }
        vm.setSeparatedContacts(TAPUtils.getInstance().separateContactsByInitial(vm.getFilteredContacts()));
        contactListAdapter.setItems(vm.getSeparatedContacts());
    }

    private void btnStartLoadingState() {
        tvButtonAddMembers.setVisibility(View.GONE);
        ivLoadingProgressAddMembers.setVisibility(View.VISIBLE);
        TAPUtils.getInstance().rotateAnimateInfinitely(this, ivLoadingProgressAddMembers);
    }

    private void btnStopLoadingState() {
        tvButtonAddMembers.setVisibility(View.VISIBLE);
        ivLoadingProgressAddMembers.setVisibility(View.GONE);
        TAPUtils.getInstance().stopViewAnimation(ivLoadingProgressAddMembers);
    }

    private TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            etSearch.removeTextChangedListener(this);
            if (s.length() == 0) {
                ivButtonClearText.setVisibility(View.GONE);
            } else {
                ivButtonClearText.setVisibility(View.VISIBLE);
            }
            updateFilteredContacts(s.toString().toLowerCase());
            etSearch.addTextChangedListener(this);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextView.OnEditorActionListener searchEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            updateFilteredContacts(etSearch.getText().toString().toLowerCase());
            TAPUtils.getInstance().dismissKeyboard(TAPAddMembersActivity.this);
            return true;
        }
    };

    private Runnable waitAnimationsToFinishRunnable = new Runnable() {
        @Override
        public void run() {
            if (rvGroupMembers.isAnimating() && null != rvGroupMembers.getItemAnimator()) {
                // RecyclerView is still animating
                rvGroupMembers.getItemAnimator().isRunning(() -> new Handler().post(waitAnimationsToFinishRunnable));
            } else {
                // RecyclerView has finished animating
                selectedMembersAdapter.setAnimating(false);
                updateSelectedMemberDecoration();
            }
        }
    };

    private TAPDefaultDataView<TAPCreateRoomResponse> addMemberView = new TAPDefaultDataView<TAPCreateRoomResponse>() {
        @Override
        public void startLoading() {
            btnStartLoadingState();
        }

        @Override
        public void endLoading() {
            super.endLoading();
        }

        @Override
        public void onSuccess(TAPCreateRoomResponse response) {
            TAPGroupManager.Companion.getGetInstance().updateGroupDataFromResponse(response);

            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(GROUP_MEMBERS, new ArrayList<>(response.getParticipants()));
            setResult(RESULT_OK, intent);
            finish();
            btnStopLoadingState();
        }

        @Override
        public void onError(TAPErrorModel error) {
            super.onError(error);
            btnStopLoadingState();
            new TapTalkDialog.Builder(TAPAddMembersActivity.this)
                    .setDialogType(TapTalkDialog.DialogType.ERROR_DIALOG)
                    .setTitle(getString(R.string.tap_error))
                    .setMessage(error.getMessage())
                    .setPrimaryButtonTitle(getString(R.string.tap_ok))
//                    .setPrimaryButtonListener(v -> onBackPressed())
                    .show();
        }

        @Override
        public void onError(String errorMessage) {
            super.onError(errorMessage);
            btnStopLoadingState();
            new TapTalkDialog.Builder(TAPAddMembersActivity.this)
                    .setDialogType(TapTalkDialog.DialogType.ERROR_DIALOG)
                    .setTitle(getString(R.string.tap_error))
                    .setMessage(getString(R.string.tap_error_message_general))
                    .setPrimaryButtonTitle(getString(R.string.tap_ok))
//                    .setPrimaryButtonListener(v -> onBackPressed())
                    .show();
        }
    };
}
