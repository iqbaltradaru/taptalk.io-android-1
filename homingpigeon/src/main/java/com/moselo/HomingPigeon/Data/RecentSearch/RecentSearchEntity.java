package com.moselo.HomingPigeon.Data.RecentSearch;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "Recent_Search")
public class RecentSearchEntity {
    @PrimaryKey @NonNull @ColumnInfo(name = "SearchText") private String searchText;
    @ColumnInfo(name = "Created") private long created;

    @Ignore
    public RecentSearchEntity(@NonNull String searchText, long created) {
        this.searchText = searchText;
        this.created = created;
    }

    public RecentSearchEntity() {
    }

    @NonNull
    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(@NonNull String searchText) {
        this.searchText = searchText;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}