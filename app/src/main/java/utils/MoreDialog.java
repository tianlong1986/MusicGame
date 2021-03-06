package utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.LinearLayout;

import com.example.gy.musicgame.R;

/**
 * Created by Administrator on 2017/10/18.
 */

public class MoreDialog {
    private static Dialog dialog = null;
    public static LinearLayout find;
    public static LinearLayout cancel;
    public static LinearLayout add;

    public static void show(Context context) {
        if (dialog == null) {
            dialog = new Dialog(context, R.style.dialog);
            dialog.setContentView(R.layout.activity_more_dialog);
            find = dialog.findViewById(R.id.find);
            cancel = dialog.findViewById(R.id.cancel);
            add = dialog.findViewById(R.id.add_lin);
        }
        dialog.show();
    }

    public static void hidden() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
