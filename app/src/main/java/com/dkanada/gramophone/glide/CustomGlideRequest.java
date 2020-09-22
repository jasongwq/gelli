package com.dkanada.gramophone.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.palette.BitmapPaletteCrossFadeFactory;
import com.dkanada.gramophone.glide.palette.BitmapPaletteWrapper;

import org.jellyfin.apiclient.model.dto.ImageOptions;
import org.jellyfin.apiclient.model.entities.ImageType;

import static com.bumptech.glide.GenericTransitionOptions.with;

public class CustomGlideRequest {
    public static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.ALL;
    public static final int DEFAULT_IMAGE = R.drawable.default_album_art;

    public static class Builder {
        final RequestManager requestManager;
        final String item;

        private Builder(@NonNull RequestManager requestManager, String item) {
            requestManager.applyDefaultRequestOptions(createRequestOptions(item));

            this.requestManager = requestManager;
            this.item = item;
        }

        public static Builder from(@NonNull RequestManager requestManager, String item) {
            return new Builder(requestManager, item);
        }

        public PaletteBuilder palette(Context context) {
            return new PaletteBuilder(this, context);
        }

        public BitmapBuilder bitmap() {
            return new BitmapBuilder(this);
        }

        public RequestBuilder<Drawable> build() {
            Object uri = item != null ? createUrl(item) : DEFAULT_IMAGE;

            return requestManager.load(uri)
                    .transition(DrawableTransitionOptions.withCrossFade());
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            Object uri = builder.item != null ? createUrl(builder.item) : DEFAULT_IMAGE;

            return builder.requestManager.asBitmap().load(uri)
                    .transition(BitmapTransitionOptions.withCrossFade());
        }
    }

    public static class PaletteBuilder {
        final Context context;
        private final Builder builder;

        public PaletteBuilder(Builder builder, Context context) {
            this.builder = builder;
            this.context = context;
        }

        public RequestBuilder<BitmapPaletteWrapper> build() {
            Object uri = builder.item != null ? createUrl(builder.item) : DEFAULT_IMAGE;

            return builder.requestManager.as(BitmapPaletteWrapper.class).load(uri)
                    .transition(with(new BitmapPaletteCrossFadeFactory()));
        }
    }

    public static RequestOptions createRequestOptions(String item) {
        return new RequestOptions()
                .centerCrop()
                .error(DEFAULT_IMAGE)
                .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                .signature(new ObjectKey(item != null ? item : 0));
    }

    public static String createUrl(String item) {
        ImageOptions options = new ImageOptions();
        options.setImageType(ImageType.Primary);
        options.setMaxHeight(800);

        return App.getApiClient().GetImageUrl(item, options);
    }
}
