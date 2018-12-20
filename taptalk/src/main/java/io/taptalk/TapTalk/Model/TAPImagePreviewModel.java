package io.taptalk.TapTalk.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class TAPImagePreviewModel implements Parcelable {
    private Uri imageUris;
    private boolean isSelected;
    private String imageCaption;

    public TAPImagePreviewModel(Uri imageUris, boolean isSelected) {
        this.imageUris = imageUris;
        this.isSelected = isSelected;
    }

    public static TAPImagePreviewModel Builder(Uri imageUris, boolean isSelected) {
        return new TAPImagePreviewModel(imageUris, isSelected);
    }

    public Uri getImageUris() {
        return imageUris;
    }

    public void setImageUris(Uri imageUris) {
        this.imageUris = imageUris;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getImageCaption() {
        return imageCaption;
    }

    public void setImageCaption(String imageCaption) {
        this.imageCaption = imageCaption;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.imageUris, flags);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeString(this.imageCaption);
    }

    protected TAPImagePreviewModel(Parcel in) {
        this.imageUris = in.readParcelable(Uri.class.getClassLoader());
        this.isSelected = in.readByte() != 0;
        this.imageCaption = in.readString();
    }

    public static final Creator<TAPImagePreviewModel> CREATOR = new Creator<TAPImagePreviewModel>() {
        @Override
        public TAPImagePreviewModel createFromParcel(Parcel source) {
            return new TAPImagePreviewModel(source);
        }

        @Override
        public TAPImagePreviewModel[] newArray(int size) {
            return new TAPImagePreviewModel[size];
        }
    };
}
