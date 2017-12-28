package utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gy.musicgame.R;

/**
 * Created by Administrator on 2017/9/7.
 */

public class ToastUtils extends Toast {
    private static Toast mToast;

    public ToastUtils(Context context) {
        super(context);
    }

    public static Toast makeText(Context context, int imageId, String msg, int duration) {
        Toast toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.activity_toast, null);
        TextView toast_image = (TextView) view.findViewById(R.id.toast_image);
        TextView toast_text = (TextView) view.findViewById(R.id.toast_text);
        toast_image.setBackgroundResource(imageId);
        toast_text.setText(msg);
        toast.setView(view);
        toast.setDuration(duration);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        return toast;
    }

    public static void showToast(Context context, int imageId, String content) {
        mToast = ToastUtils.makeText(context, imageId, content, 100);
        mToast.show();
    }
}
