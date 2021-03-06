package com.ohiostate.chuckmyphone.chuckmyphone;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordFragment extends Fragment implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();

    private boolean actionPending;

    private OnFragmentInteractionListener mListener;

    // Views
    private Button confirmButton;
    private Button cancelButton;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText newPasswordConfirmationEditText;

    public static ChangePasswordFragment newInstance() {
        return new ChangePasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionPending = false;
        Log.d(TAG, "onCreate() called");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_change_password, container, false);

        initializeViews(view);
        MiscHelperMethods.setupUI(view, getActivity());

        return view;
    }

    private void initializeViews(View view){
        oldPasswordEditText = (EditText) view.findViewById(R.id.change_password_old_password_edit_text);
        newPasswordEditText = (EditText) view.findViewById(R.id.change_password_new_password_edit_text);
        newPasswordConfirmationEditText = (EditText) view.findViewById(R.id.change_password_new_password_confirmation_edit_text);

        confirmButton = (Button) view.findViewById(R.id.change_password_confirm_button);
        confirmButton.setOnClickListener(this);
        cancelButton = (Button) view.findViewById(R.id.change_password_cancel_button);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onClick(View v) {
        if (!actionPending) {
            switch (v.getId()) {
                case R.id.change_password_confirm_button:
                    attemptChangePassword();
                    break;
                default:
                    getActivity().onBackPressed();
                    break;
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Loading your previous request, please wait", Toast.LENGTH_LONG).show();
        }
    }

    private boolean attemptChangePassword() {
        String oldPassword = oldPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String newPasswordConfirmation = newPasswordConfirmationEditText.getText().toString();
        if (MiscHelperMethods.isNetworkAvailable(getActivity())) {
            if (!newPasswordConfirmation.equals("") && !newPassword.equals("")) {
                if (newPassword.equals(newPasswordConfirmation)) {
                    if (SharedPreferencesHelper.getPassword(getActivity().getApplicationContext()).equals(oldPassword)) {
                        Toast.makeText(getActivity().getApplicationContext(), "Changing password, please wait", Toast.LENGTH_SHORT).show();
                        actionPending = true;

                        SharedPreferencesHelper.setPassword(getActivity().getApplicationContext(), newPasswordConfirmation);
                        FirebaseHelper.getInstance().changePassword(newPasswordConfirmation, this);
                        return true;
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Your old password is incorrect, please re-type it", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Your new password entries don't match, please re-type them", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Please enter your new password", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Cannot change password when you have no internet", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void onSuccessfulPasswordChange() {
        Toast.makeText(getActivity().getApplicationContext(), "Password was changed!", Toast.LENGTH_LONG).show();
        actionPending = false;

        //startActivity(new Intent(getActivity().getApplication(), MainActivity.class));
        getActivity().onBackPressed();

    }

    public void onUnsuccessfulPasswordChange(Exception e) {
        actionPending = false;
        Toast.makeText(getActivity().getApplicationContext(), "Password was not changed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}