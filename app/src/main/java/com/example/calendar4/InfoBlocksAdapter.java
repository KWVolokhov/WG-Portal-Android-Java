package com.example.calendar4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

/**
 * Task 100: RecyclerView adapter of InfoFieldView.
 * Item types: 0 = editable text block, 1 = image thumbnail, 2 = video thumbnail
 * (frame + play icon, no autoplay), 3 = file row. Images/videos are sized to
 * 1/4 of the row width; a tap on an item opens the standard Android viewer.
 */
class InfoBlocksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final InfoFieldView host;

    InfoBlocksAdapter(InfoFieldView host) {
        this.host = host;
    }

    @Override
    public int getItemCount() { return host.blocks.size(); }

    @Override
    public int getItemViewType(int position) {
        String t = host.blocks.get(position).type;
        if ("img".equals(t)) return 1;
        if ("vid".equals(t)) return 2;
        if ("file".equals(t)) return 3;
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        if (viewType == 0) return new TextHolder(makeTextEdit(ctx));
        if (viewType == 3) return makeFileHolder(ctx);
        return new MediaHolder(makeMediaFrame(ctx), viewType == 2);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        InfoFieldView.Block b = host.blocks.get(position);
        if (h instanceof TextHolder) ((TextHolder) h).bind(b);
        else if (h instanceof MediaHolder) ((MediaHolder) h).bind(b, host.recyclerWidth());
        else if (h instanceof FileHolder) ((FileHolder) h).bind(b);
    }

    /** Removes a block (delete buttons of the media/file items). */
    private void removeAt(int pos) {
        host.blocks.remove(pos);
        notifyItemRemoved(pos);
        host.updateCollapsed();
    }

    private EditText makeTextEdit(Context ctx) {
        EditText et = new EditText(ctx);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et.setSingleLine(false);
        et.setMinLines(1);
        et.setGravity(Gravity.START | Gravity.TOP);
        et.setBackgroundResource(R.drawable.bg_underline);
        et.setHint(host.hint);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = host.dpToPx(4);
        et.setLayoutParams(lp);
        return et;
    }

    /** Editable text block; the current content is written back into the block on every change. */
    private class TextHolder extends RecyclerView.ViewHolder {
        final EditText et;
        InfoFieldView.Block bound;
        boolean binding;

        TextHolder(EditText et) {
            super(et);
            this.et = et;
            et.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {
                    if (binding || bound == null) return;
                    bound.isHtml = true;
                    bound.text = Html.toHtml(s);
                }
            });
        }

        void bind(InfoFieldView.Block b) {
            binding = true;
            bound = b;
            et.setText(b.isHtml ? Html.fromHtml(b.text == null ? "" : b.text)
                    : (b.text == null ? "" : b.text));
            binding = false;
        }
    }

    private FrameLayout makeMediaFrame(Context ctx) {
        FrameLayout frame = new FrameLayout(ctx);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = host.dpToPx(8);
        frame.setLayoutParams(lp);
        return frame;
    }

    /** Image/video thumbnail: 1/4 of the row width; video = frame + play icon, no autoplay. */
    private class MediaHolder extends RecyclerView.ViewHolder {
        final FrameLayout frame;
        final ImageView image;
        final ImageButton delete;
        InfoFieldView.Block bound;

        MediaHolder(FrameLayout frame, boolean video) {
            super(frame);
            this.frame = frame;
            Context ctx = frame.getContext();
            image = new ImageView(ctx);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setBackgroundResource(R.drawable.bg_underline);
            frame.addView(image, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            if (video) {
                ImageView play = new ImageView(ctx);
                play.setImageResource(android.R.drawable.ic_media_play);
                play.setLayoutParams(new FrameLayout.LayoutParams(
                        host.dpToPx(28), host.dpToPx(28), Gravity.CENTER));
                frame.addView(play);
            }

            delete = new ImageButton(ctx);
            delete.setImageResource(R.drawable.ic_delete);
            delete.setBackgroundColor(Color.TRANSPARENT);
            delete.setContentDescription("Удалить вложение");
            delete.setLayoutParams(new FrameLayout.LayoutParams(
                    host.dpToPx(24), host.dpToPx(24), Gravity.END | Gravity.TOP));
            frame.addView(delete);

            image.setOnClickListener(v -> {
                if (bound != null) AttachmentStore.openExternal(ctx, bound.file);
            });
            delete.setOnClickListener(v -> {
                int pos = host.blocks.indexOf(bound);
                if (pos >= 0) removeAt(pos);
            });
        }

        void bind(InfoFieldView.Block b, int rowWidth) {
            bound = b;
            int side = rowWidth > 0 ? rowWidth / 4 : host.dpToPx(80);
            ViewGroup.LayoutParams lp = frame.getLayoutParams();
            lp.width = side;
            lp.height = side;
            frame.setLayoutParams(lp);
            image.setImageBitmap(host.thumbFor(b));
        }
    }

    private FileHolder makeFileHolder(Context ctx) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = host.dpToPx(8);
        row.setLayoutParams(lp);

        ImageView icon = new ImageView(ctx);
        icon.setImageResource(R.drawable.ic_attach_file);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                host.dpToPx(24), host.dpToPx(24));
        ilp.rightMargin = host.dpToPx(8);
        icon.setLayoutParams(ilp);
        row.addView(icon);

        TextView tvName = new TextView(ctx);
        tvName.setTextSize(16);
        tvName.setSingleLine(true);
        tvName.setEllipsize(TextUtils.TruncateAt.END);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvName);

        ImageButton delete = new ImageButton(ctx);
        delete.setImageResource(R.drawable.ic_delete);
        delete.setBackgroundColor(Color.TRANSPARENT);
        delete.setContentDescription("Удалить вложение");
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                host.dpToPx(24), host.dpToPx(24));
        dlp.leftMargin = host.dpToPx(8);
        delete.setLayoutParams(dlp);
        row.addView(delete);

        return new FileHolder(row, tvName, delete);
    }

    /** File row: icon + name; a tap opens the standard Android viewer. */
    private class FileHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final ImageButton delete;
        InfoFieldView.Block bound;

        FileHolder(LinearLayout row, TextView tvName, ImageButton delete) {
            super(row);
            this.tvName = tvName;
            this.delete = delete;
            row.setOnClickListener(v -> {
                if (bound != null) AttachmentStore.openExternal(host.getContext(), bound.file);
            });
            delete.setOnClickListener(v -> {
                int pos = host.blocks.indexOf(bound);
                if (pos >= 0) removeAt(pos);
            });
        }

        void bind(InfoFieldView.Block b) {
            bound = b;
            String label = (b.name != null && !b.name.isEmpty()) ? b.name : b.file;
            tvName.setText(label);
        }
    }

    /** Decodes (and caches) the thumbnail of an image/video block. */
    private Bitmap thumbFor(InfoFieldView.Block b) {
        String key = b.type + ":" + b.file;
        Bitmap cached = host.thumbs.get(key);
        if (cached != null) return cached;
        Bitmap bmp = null;
        try {
            File f = AttachmentStore.getFile(host.getContext(), b.file);
            if (f != null && f.exists()) {
                if ("img".equals(b.type)) bmp = decodeScaled(f, host.dpToPx(120));
                else if ("vid".equals(b.type)) bmp = videoFrame(f);
            }
        } catch (Exception e) {
            bmp = null;
        }
        host.thumbs.put(key, bmp);
        return bmp;
    }

    /** First frame of a video (preview only - the video itself never autoplays). */
    private Bitmap videoFrame(File f) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(f.getAbsolutePath());
            return mmr.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            return null;
        } finally {
            try { mmr.release(); } catch (Exception e) { }
        }
    }

    private Bitmap decodeScaled(File f, int target) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), o);
        int sample = 1;
        while (o.outWidth / (sample * 2) >= target) sample *= 2;
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = sample;
        return BitmapFactory.decodeFile(f.getAbsolutePath(), o2);
    }
}
