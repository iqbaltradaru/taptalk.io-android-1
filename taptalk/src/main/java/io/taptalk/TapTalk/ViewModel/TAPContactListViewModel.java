package io.taptalk.TapTalk.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.taptalk.TapTalk.Manager.TAPDataManager;
import io.taptalk.TapTalk.Model.ResponseModel.TapContactListModel;
import io.taptalk.TapTalk.Model.TAPImageURL;
import io.taptalk.TapTalk.Model.TAPUserModel;

public class TAPContactListViewModel extends AndroidViewModel {

    // Contact List needs to be sorted ascending by name
    private LiveData<List<TAPUserModel>> contactListLive;
    private List<TAPUserModel> contactList;
    private List<TAPUserModel> filteredContacts;
    private List<TAPUserModel> selectedContacts;
    private List<TAPUserModel> existingMembers;
    private List<String> selectedContactsIds;
    @Deprecated private List<List<TAPUserModel>> separatedContacts;
    private List<TapContactListModel> separatedContactList;
    private List<TapContactListModel> newChatMenuList;
    private List<TapContactListModel> adapterItems;
    private TapContactListModel infoLabelItem;
    private TAPImageURL groupImage;
    private String groupName;
    private String roomID;
    private String pendingSearch;
    private boolean isSelecting;
    private boolean isFirstContactSyncDone;
    private int initialGroupSize = 0;
    private int groupAction;

    public TAPContactListViewModel(@NonNull Application application) {
        super(application);
        contactListLive = TAPDataManager.getInstance().getMyContactList();
    }

    public LiveData<List<TAPUserModel>> getContactListLive() {
        return contactListLive;
    }

    public List<TAPUserModel> getContactList() {
        return contactList == null ? contactList = new ArrayList<>() : contactList;
    }

    public void setContactList(List<TAPUserModel> contactList) {
        this.contactList = contactList;
    }

    public List<TAPUserModel> getFilteredContacts() {
        return filteredContacts == null ? filteredContacts = new ArrayList<>() : filteredContacts;
    }

    public void setFilteredContacts(List<TAPUserModel> filteredContacts) {
        this.filteredContacts = filteredContacts;
    }

    public List<TAPUserModel> getSelectedContacts() {
        return selectedContacts == null ? selectedContacts = new ArrayList<>() : selectedContacts;
    }

    public void setSelectedContacts(List<TAPUserModel> selectedContacts) {
        this.selectedContacts = selectedContacts;
    }

    public void setContactListLive(LiveData<List<TAPUserModel>> contactListLive) {
        this.contactListLive = contactListLive;
    }

    public List<String> getSelectedContactsIds() {
        return selectedContactsIds == null ? selectedContactsIds = new ArrayList<>() : selectedContactsIds;
    }

    public void setSelectedContactsIds(List<String> selectedContactsIds) {
        this.selectedContactsIds = selectedContactsIds;
    }

    public List<TAPUserModel> getExistingMembers() {
        return existingMembers == null ? existingMembers = new ArrayList<>() : existingMembers;
    }

    public void setExistingMembers(List<TAPUserModel> existingMembers) {
        this.existingMembers = existingMembers;
    }

    public List<List<TAPUserModel>> getSeparatedContacts() {
        return separatedContacts == null ? separatedContacts = new ArrayList<>() : separatedContacts;
    }

    public void setSeparatedContacts(List<List<TAPUserModel>> separatedContacts) {
        this.separatedContacts = separatedContacts;
    }

    public List<TapContactListModel> getSeparatedContactList() {
        return null == separatedContactList ? separatedContactList = new ArrayList<>() : separatedContactList;
    }

    public void setSeparatedContactList(List<TapContactListModel> separatedContactList) {
        this.separatedContactList = separatedContactList;
    }

    public List<TapContactListModel> getNewChatMenuList() {
        return null == newChatMenuList ? newChatMenuList = new ArrayList<>() : newChatMenuList;
    }

    public void setNewChatMenuList(List<TapContactListModel> newChatMenuList) {
        this.newChatMenuList = newChatMenuList;
    }

    public List<TapContactListModel> getAdapterItems() {
        return null == adapterItems ? adapterItems = new ArrayList<>() : adapterItems;
    }

    public void refreshAdapterItems() {
        if (!getAdapterItems().isEmpty()) {
            getAdapterItems().clear();
        }
        getAdapterItems().addAll(getNewChatMenuList());
        getAdapterItems().addAll(getSeparatedContactList());
        if (null != getInfoLabelItem()) {
            getAdapterItems().add(getInfoLabelItem());
        }
    }

    public TapContactListModel getInfoLabelItem() {
        return infoLabelItem;
    }

    public void setInfoLabelItem(TapContactListModel infoLabelItem) {
        this.infoLabelItem = infoLabelItem;
    }

    public TAPImageURL getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(TAPImageURL groupImage) {
        this.groupImage = groupImage;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getPendingSearch() {
        return pendingSearch;
    }

    public void setPendingSearch(String pendingSearch) {
        this.pendingSearch = pendingSearch;
    }

    public boolean isSelecting() {
        return isSelecting;
    }

    public void setSelecting(boolean selecting) {
        isSelecting = selecting;
    }

    public boolean isFirstContactSyncDone() {
        return isFirstContactSyncDone;
    }

    public void setFirstContactSyncDone(boolean firstContactSyncDone) {
        isFirstContactSyncDone = firstContactSyncDone;
    }

    public int getInitialGroupSize() {
        return initialGroupSize;
    }

    public void setInitialGroupSize(int initialGroupSize) {
        this.initialGroupSize = initialGroupSize;
    }

    public int getGroupAction() {
        return groupAction;
    }

    public void setGroupAction(int groupAction) {
        this.groupAction = groupAction;
    }

    public void addSelectedContact(TAPUserModel contactModel) {
        getSelectedContacts().add(contactModel);
        getSelectedContactsIds().add(contactModel.getUserID());
    }

    public void removeSelectedContact(TAPUserModel contactModel) {
        getSelectedContacts().remove(contactModel);
        getSelectedContactsIds().remove(contactModel.getUserID());
    }
}
