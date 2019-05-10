package io.taptalk.TapTalk.View.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

import io.taptalk.TapTalk.Data.Message.TAPMessageEntity;
import io.taptalk.TapTalk.Helper.OverScrolled.OverScrollDecoratorHelper;
import io.taptalk.TapTalk.Helper.TAPUtils;
import io.taptalk.TapTalk.Interface.TapTalkRoomListInterface;
import io.taptalk.TapTalk.Listener.TAPDatabaseListener;
import io.taptalk.TapTalk.Manager.TAPChatManager;
import io.taptalk.TapTalk.Manager.TAPDataManager;
import io.taptalk.TapTalk.Model.TAPImageURL;
import io.taptalk.TapTalk.Model.TAPRoomModel;
import io.taptalk.TapTalk.Model.TAPSearchChatModel;
import io.taptalk.TapTalk.Model.TAPUserModel;
import io.taptalk.TapTalk.View.Adapter.TAPSearchChatAdapter;
import io.taptalk.TapTalk.ViewModel.TAPSearchChatViewModel;
import io.taptalk.Taptalk.R;

import static io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.MESSAGE;
import static io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.ROOM;
import static io.taptalk.TapTalk.Model.TAPSearchChatModel.Type.EMPTY_STATE;
import static io.taptalk.TapTalk.Model.TAPSearchChatModel.Type.ROOM_ITEM;
import static io.taptalk.TapTalk.Model.TAPSearchChatModel.Type.SECTION_TITLE;

public class TAPForwardPickerActivity extends TAPBaseActivity {

    private ConstraintLayout clActionBar, clSearchBar;
    private ImageView ivButtonClose, ivButtonSearch, ivButtonBack, ivButtonCancel;
    private EditText etSearch;
    private RecyclerView rvForwardList;

    private TAPSearchChatViewModel vm;
    private TAPSearchChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tap_activity_forward_picker);

        initViewModel();
        initView();
        setRecentChatsFromDatabase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.tap_stay, R.anim.tap_slide_down);
    }

    private void initViewModel() {
        vm = ViewModelProviders.of(this).get(TAPSearchChatViewModel.class);
        vm.setSelectedMessage(getIntent().getParcelableExtra(MESSAGE));
    }

    private void initView() {
        clActionBar = findViewById(R.id.cl_action_bar);
        clSearchBar = findViewById(R.id.cl_search_bar);
        ivButtonClose = findViewById(R.id.iv_button_close);
        ivButtonSearch = findViewById(R.id.iv_button_search);
        ivButtonBack = findViewById(R.id.iv_button_back);
        ivButtonCancel = findViewById(R.id.iv_button_cancel);
        etSearch = findViewById(R.id.et_search);
        rvForwardList = findViewById(R.id.rv_forward_list);

        etSearch.addTextChangedListener(searchTextWatcher);

        ivButtonClose.setOnClickListener(v -> onBackPressed());
        ivButtonSearch.setOnClickListener(v -> showSearchBar());
        ivButtonBack.setOnClickListener(v -> showActionBar());
        ivButtonCancel.setOnClickListener(v -> clearSearch());

        adapter = new TAPSearchChatAdapter(vm.getSearchResults(), roomListInterface);
        rvForwardList.setAdapter(adapter);
        rvForwardList.setHasFixedSize(false);
        rvForwardList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        OverScrollDecoratorHelper.setUpOverScroll(rvForwardList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        rvForwardList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                TAPUtils.getInstance().dismissKeyboard(TAPForwardPickerActivity.this);
            }
        });
    }

    private void showActionBar() {
        clActionBar.setVisibility(View.VISIBLE);
        clSearchBar.setVisibility(View.GONE);
        clearSearch();
//        etSearch.clearFocus();
        TAPUtils.getInstance().dismissKeyboard(this);
    }

    private void showSearchBar() {
        clActionBar.setVisibility(View.GONE);
        clSearchBar.setVisibility(View.VISIBLE);
        TAPUtils.getInstance().showKeyboard(this, etSearch);
    }

    private void clearSearch() {
        etSearch.setText("");
    }

    private void startSearch() {
        if (etSearch.getText().toString().equals(" ")) {
            // Clear keyword when EditText only contains a space
            etSearch.setText("");
            return;
        }

        vm.clearSearchResults();
        vm.setSearchKeyword(etSearch.getText().toString().toLowerCase().trim());
        adapter.setSearchKeyword(vm.getSearchKeyword());
        if (vm.getSearchKeyword().isEmpty()) {
            showRecentChats();
        } else {
            TAPDataManager.getInstance().searchAllRoomsFromDatabase(vm.getSearchKeyword(), roomSearchListener);
            //flag untuk nandain kalau skrg lagi tidak munculin halaman recent Search
            vm.setRecentSearchShown(false);
        }
    }

    private void setRecentChatsFromDatabase() {
        TAPDataManager.getInstance().getRoomList(false, new TAPDatabaseListener<TAPMessageEntity>() {
            @Override
            public void onSelectFinished(List<TAPMessageEntity> entities) {
                if (null != entities && entities.size() > 0) {
                    TAPSearchChatModel recentTitleItem = new TAPSearchChatModel(SECTION_TITLE);
                    recentTitleItem.setSectionTitle(getString(R.string.tap_recent_chats));
                    vm.addRecentSearches(recentTitleItem);

                    for (TAPMessageEntity entity : entities) {
                        TAPSearchChatModel recentItem = new TAPSearchChatModel(ROOM_ITEM);
                        TAPRoomModel roomModel = new TAPRoomModel(
                                entity.getRoomID(),
                                entity.getRoomName(),
                                entity.getRoomType(),
                                TAPUtils.getInstance().fromJSON(new TypeReference<TAPImageURL>() {
                                }, entity.getRoomImage()),
                                entity.getRoomColor());
                        recentItem.setRoom(roomModel);
                        vm.addRecentSearches(recentItem);
                    }
                }
                showRecentChats();
            }
        });
    }

    private void showRecentChats() {
        runOnUiThread(() -> adapter.setItems(vm.getRecentSearches(), false));
    }

    private void setEmptyState() {
        vm.clearSearchResults();
        vm.addSearchResult(new TAPSearchChatModel(EMPTY_STATE));
        runOnUiThread(() -> adapter.setItems(vm.getSearchResults(), false));
    }

    private TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            startSearch();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TapTalkRoomListInterface roomListInterface = new TapTalkRoomListInterface() {
        @Override
        public void onRoomSelected(TAPRoomModel roomModel) {
            Intent intent = new Intent();
            intent.putExtra(MESSAGE, vm.getSelectedMessage());
            intent.putExtra(ROOM, roomModel);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private TAPDatabaseListener<TAPMessageEntity> roomSearchListener = new TAPDatabaseListener<TAPMessageEntity>() {
        @Override
        public void onSelectedRoomList(List<TAPMessageEntity> entities, Map<String, Integer> unreadMap) {
            if (entities.size() > 0) {
                TAPSearchChatModel sectionTitleChatsAndContacts = new TAPSearchChatModel(SECTION_TITLE);
                sectionTitleChatsAndContacts.setSectionTitle(getString(R.string.tap_chats_and_contacts));
                vm.addSearchResult(sectionTitleChatsAndContacts);
                for (TAPMessageEntity entity : entities) {
                    TAPSearchChatModel result = new TAPSearchChatModel(ROOM_ITEM);
                    // Convert message to room model
                    TAPRoomModel room = new TAPRoomModel(
                            entity.getRoomID(),
                            entity.getRoomName(),
                            entity.getRoomType(),
                            // TODO: 18 October 2018 REMOVE CHECK
                            /* TEMPORARY CHECK FOR NULL IMAGE */null != entity.getRoomImage() ?
                            TAPUtils.getInstance().fromJSON(new TypeReference<TAPImageURL>() {
                            }, entity.getRoomImage())
                            /* TEMPORARY CHECK FOR NULL IMAGE */ : null,
                            entity.getRoomColor());
                    room.setUnreadCount(unreadMap.get(room.getRoomID()));
                    result.setRoom(room);
                    vm.addSearchResult(result);
                }
                runOnUiThread(() -> {
                    adapter.setItems(vm.getSearchResults(), false);
                    TAPDataManager.getInstance().searchAllMyContacts(vm.getSearchKeyword(), contactSearchListener);
                });
            } else {
                TAPDataManager.getInstance().searchAllMyContacts(vm.getSearchKeyword(), contactSearchListener);
            }
        }
    };

    private TAPDatabaseListener<TAPUserModel> contactSearchListener = new TAPDatabaseListener<TAPUserModel>() {
        @Override
        public void onSelectFinished(List<TAPUserModel> entities) {
            if (entities.size() > 0) {
                if (vm.getSearchResults().size() == 0) {
                    TAPSearchChatModel sectionTitleChatsAndContacts = new TAPSearchChatModel(SECTION_TITLE);
                    sectionTitleChatsAndContacts.setSectionTitle(getString(R.string.tap_chats_and_contacts));
                    vm.addSearchResult(sectionTitleChatsAndContacts);
                }
                for (TAPUserModel contact : entities) {
                    TAPSearchChatModel result = new TAPSearchChatModel(ROOM_ITEM);
                    // Convert contact to room model
                    // TODO: 18 October 2018 LENGKAPIN DATA
                    TAPRoomModel room = new TAPRoomModel(
                            TAPChatManager.getInstance().arrangeRoomId(TAPChatManager.getInstance().getActiveUser().getUserID(), contact.getUserID()),
                            contact.getName(),
                            /* 1 ON 1 ROOM TYPE */ 1,
                            contact.getAvatarURL(),
                            /* SET DEFAULT ROOM COLOR*/""
                    );
                    // Check if result already contains contact from chat room query
                    if (!vm.resultContainsRoom(room.getRoomID())) {
                        result.setRoom(room);
                        vm.addSearchResult(result);
                    }
                }
                vm.getSearchResults().get(vm.getSearchResults().size() - 1).setLastInSection(true);
                runOnUiThread(() -> {
                    adapter.setItems(vm.getSearchResults(), false);
                });
            } else if (vm.getSearchResults().size() == 0) {
                setEmptyState();
            }
        }
    };
}