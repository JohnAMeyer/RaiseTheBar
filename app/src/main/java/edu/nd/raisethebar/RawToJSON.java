package edu.nd.raisethebar;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.R.attr.lines;
import static android.R.id.input;

/**
 * Created by jack1 on 10/19/2016.
 */

public class RawToJSON {
    public static JSONObject toJSON(Context ctx, int resId) throws IOException, JSONException {
        BufferedReader input = new BufferedReader(new InputStreamReader(ctx.getResources().openRawResource(resId)));
            String line, lines = "";
            while ((line = input.readLine()) != null) {
                lines += line + "\n";
            }
        return new JSONObject(lines);
    }
}
