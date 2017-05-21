package com.gambino_serra.KIU;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * la classe modella l'interazione tra Activity e le ProgressDialog.
 */
public abstract class BaseActivity extends AppCompatActivity {


    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            setMessage();
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected abstract void setMessage();

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

}