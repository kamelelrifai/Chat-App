package com.example.lapitchat.view.fragment.userCycle;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.lapitchat.R;
import com.example.lapitchat.helper.LoadingDialog;
import com.example.lapitchat.view.activity.MainActivity;
import com.example.lapitchat.view.fragment.BaseFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.example.lapitchat.helper.HelperMethods.replaceFragment;

public class RegisterFragment extends BaseFragment {
    @BindView(R.id.register_fragment_ll)
    LinearLayout registerFragmentLl;
    private FirebaseAuth mAuth;
    Unbinder unbinder;
    @BindView(R.id.register_fragment_til_name)
    TextInputLayout registerFragmentTilName;
    @BindView(R.id.register_fragment_til_email)
    TextInputLayout registerFragmentTilEmail;
    @BindView(R.id.register_fragment_til_password)
    TextInputLayout registerFragmentTilPassword;
    @BindView(R.id.register_fragment_btn_create)
    Button registerFragmentBtnCreate;
    private DatabaseReference database;
private LoadingDialog loadingDialog ;
    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        unbinder = ButterKnife.bind(this, view);
        mAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(getActivity());
        setUpActivity();
        startActivity.setToolBar(view.VISIBLE, getString(R.string.login), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(getActivity().getSupportFragmentManager(), R.id.start_activity_frame, new StartFragment());
            }
        });
        return view;
    }

    @OnClick(R.id.register_fragment_btn_create)
    public void onViewClicked() {
        String sName = registerFragmentTilName.getEditText().getText().toString();
        String sEmail = registerFragmentTilEmail.getEditText().getText().toString();
        String sPassword = registerFragmentTilPassword.getEditText().getText().toString();
        if (!TextUtils.isEmpty(sName) || !TextUtils.isEmpty(sEmail) || !TextUtils.isEmpty(sPassword)) {
            loadingDialog.startLoadingDialog();
            registerUser(sName, sEmail, sPassword);
        }

    }

    private void registerUser(String sName, String sEmail, String sPassword) {

        mAuth.createUserWithEmailAndPassword(sEmail, sPassword)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success,
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uID = currentUser.getUid();
                            database  = FirebaseDatabase.getInstance().getReference().child("Users").child(uID);

                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("name", sName);
                            userMap.put("status","getString(R.string.avaiable)");
                            userMap.put("image","default");
                            userMap.put("thumb_image", "default");

                            database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                            loadingDialog.dismissDialog();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                                    }else {
                                        Toast.makeText(getActivity(), "aaaa", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            loadingDialog.dismissDialog();
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}
