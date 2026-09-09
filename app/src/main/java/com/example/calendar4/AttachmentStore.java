package com.example.calendar4;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Task 100: storage for the attachments (images / videos / files) referenced by
 * the JSON values of InfoFieldView.
 *
 * Attachment files live in a separate folder inside the app files dir; the folder
 * NAME is kept as a text field in the parameters (CALPARAM.AttachFolder, default
 * "Attachments"). The (already existing) database name is also kept as a text
 * field in the parameters (CALPARAM.DBName, default "WGPlanDatabase.db").
 */
public class AttachmentStore {

    private AttachmentStore() {
    }

    /** Folder for attachments (created on demand); the name comes from the parameters. */
    public static File attachmentsDir(Context context) {
        String folder = CalParamRecord.DEFAULT_ATTACH_FOLDER;
        try {
            CalParamRecord p = ManageSQLDatabase.getInstance(context).getCalParam();
            if (p != null && p.AttachFolder != null && !p.AttachFolder.trim().isEmpty()) folder = p.AttachFolder.trim();
        } catch (Exception e) {
            // Fall back to the default folder name
        }
        File dir = new File(context.getFilesDir(), folder);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** Returns the attachment file by its stored name (simple file names only). */
    public static File getFile(Context context, String name) {
        if (name == null || name.isEmpty()) return null;
        File dir = attachmentsDir(context);
        return new File(dir, new File(name).getName());
    }

    /** Copies the picked content into the attachments folder. Returns the stored file name or null. */
    public static String addFromUri(Context context, Uri uri) {
        InputStream in = null;
        OutputStream os = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
            if (in == null) return null;
            String name = uniqueName(context, uri);
            File out = new File(attachmentsDir(context), name);
            os = new FileOutputStream(out);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
            return name;
        } catch (Exception e) {
            return null;
        } finally {
            try { if (in != null) in.close(); } catch (Exception e) { }
            try { if (os != null) os.close(); } catch (Exception e) { }
        }
    }

    /** Opens the attachment with the standard Android viewer (fullscreen when possible). */
    public static void openExternal(Context context, String name) {
        try {
            File f = getFile(context, name);
            if (f == null || !f.exists()) {
                Toast.makeText(context, "Файл не найден: " + name, Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", f);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeOf(context, null, name));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Нет приложения для открытия файла", Toast.LENGTH_SHORT).show();
        }
    }

    /** Display name of the picked content (used for file rows and extensions). */
    public static String displayName(Context context, Uri uri, String fallback) {
        try {
            Cursor c = context.getContentResolver().query(uri, null, null, null, null);
            if (c != null) {
                try {
                    int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0 && c.moveToFirst()) {
                        String n = c.getString(idx);
                        if (n != null && !n.trim().isEmpty()) return n.trim();
                    }
                } finally {
                    c.close();
                }
            }
        } catch (Exception e) {
            // ignore - fallback below
        }
        return fallback;
    }

    /** MIME type of the attachment: from the content resolver or by extension. */
    public static String mimeOf(Context context, Uri uri, String name) {
        if (uri != null) {
            try {
                String t = context.getContentResolver().getType(uri);
                if (t != null && !t.isEmpty()) return t;
            } catch (Exception e) {
                // fall through to the extension mapping
            }
        }
        String ext = extensionOf(name);
        if (ext != null) {
            String m = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if (m != null) return m;
        }
        return "application/octet-stream";
    }

    /** Generates a unique file name for the picked content: yyyymmdd_hhmmss_mmm.ext */
    private static String uniqueName(Context context, Uri uri) {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        String ext = extensionOf(displayName(context, uri, ""));
        if (ext == null) {
            String m = null;
            try { m = context.getContentResolver().getType(uri); } catch (Exception e) { }
            if (m != null) ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(m);
        }
        if (ext == null) ext = "dat";
        return stamp + "." + ext;
    }

    /** Safe extension of a display name (1..5 letters/digits) or null. */
    private static String extensionOf(String name) {
        if (name == null) return null;
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return null;
        String ext = name.substring(dot + 1).toLowerCase(Locale.US);
        if (ext.isEmpty() || ext.length() > 5) return null;
        for (int i = 0; i < ext.length(); i++) {
            if (!Character.isLetterOrDigit(ext.charAt(i))) return null;
        }
        return ext;
    }
}
