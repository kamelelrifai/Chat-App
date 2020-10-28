package com.example.lapitchat.view.fragment.mainCycle.menuPackage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.lapitchat.R;
import com.example.lapitchat.helper.LoadingDialog;
import com.example.lapitchat.view.fragment.BaseFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static com.example.lapitchat.helper.HelperMethods.onLoadImageFromUrl;
import static com.example.lapitchat.helper.HelperMethods.onLoadImageFromUrlOff;
import static com.example.lapitchat.helper.HelperMethods.replaceFragment;

public class SettingsFragment extends BaseFragment {
    @BindView(R.id.settings_fragment_img)
    CircleImageView settingsFragmentImg;
    @BindView(R.id.settings_fragment_txt_display)
    TextView settingsFragmentTxtDisplay;
    @BindView(R.id.settings_fragment_txt_status)
    TextView settingsFragmentTxtStatus;
    @BindView(R.id.settings_fragment_btn_image)
    Button settingsFragmentBtnImage;
    @BindView(R.id.settings_fragment_btn_status)
    Button settingsFragmentBtnStatus;

    private DatabaseReference databaseReference;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;
    private StorageReference mStorageImageRef;

    private LoadingDialog loadingDialog;

    private static final int GALLERY_PICK = 1;
    byte[] imageByte;

    private StatusFragment statusFragment;
    private Bundle bundle;
    private Unbinder unbinder;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        // get info from this fragment and send it to another one through bundle
        bundle = new Bundle();
        statusFragment = new StatusFragment();

        mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images");
        mStorageImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String mUId = mCurrentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mUId);
        // adding firebase offline features
        databaseReference.keepSynced(true);

        //set values
        getValues(databaseReference);

        //setting activity
        setUpActivity();
        mainActivity.setToolBar(view.GONE);
        mainActivity.setFrame(view.VISIBLE);

        return view;
    }


    /**
     * this method used to set values
     *
     * @param databaseReference get database ref from current fragment
     */
    private void getValues(DatabaseReference databaseReference) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {

                    //getting values from database
                    String name = snapshot.child("name").getValue().toString();
                    String image = snapshot.child("image").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();

                    // set values
                    settingsFragmentTxtDisplay.setText(name);

                    if (!image.equals("default")) {
                        onLoadImageFromUrlOff(settingsFragmentImg, image, getActivity());
                    }

                    settingsFragmentTxtStatus.setText(status);
                } catch (Exception e) {

                }

            }//in data changed

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                try {

                } catch (Exception e) {

                }
            }
        });
    }

    // change image and status

    @OnClick({R.id.settings_fragment_btn_image, R.id.settings_fragment_btn_status})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.settings_fragment_btn_image:
                // start picker to get image for cropping and then use the image in cropping activity

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(getContext(), this);
                break;

            case R.id.settings_fragment_btn_status:

                // using bundle here to send information through fragments
                bundle.putString("STATUS_TXT", settingsFragmentTxtStatus.getText().toString());
                statusFragment.setArguments(bundle);

                // go to status fragment
                replaceFragment(getActivity().getSupportFragmentManager(), R.id.main_activity_of, statusFragment);
                break;
        }
    }
    /**
     * using it when we recieve image from cropper
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //set loading dialog
                loadingDialog = new LoadingDialog(getActivity());
                loadingDialog.startLoadingDialog();

                // get cropping image uri
                Uri resultUri = result.getUri();
                File imageFile = new File(resultUri.getPath());

                try {
                    Bitmap compressedImageBitmap = new Compressor(getContext())
                            .setMaxHeight(200).setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(imageFile);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    imageByte = baos.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                String Current_uID = mCurrentUser.getUid();
                mStorageRef = mStorageImageRef.child(Current_uID + ".jpg");
                StorageReference bitmapPath = mStorageImageRef.child("thumbs").child(Current_uID + ".jpg");
                // upload profile photo and thumb
                uploadingPhoto(mStorageRef, resultUri, bitmapPath);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadingPhoto(StorageReference mStorageRef, Uri resultUri, StorageReference bitmapPath) {

        mStorageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        databaseReference.child("image").setValue(uri.toString());
                        // start uploading thumb
                        UploadTask uploadTask = bitmapPath.putBytes(imageByte);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                try {

                                } catch (Exception e) {

                                }
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                bitmapPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        databaseReference.child("thumb_image").setValue(uri.toString());
                                    }

                                });
                            }
                        });

                    }
                });

                loadingDialog.dismissDialog();
            }
        });
    }

    @Override
    public void onBack() {
        startActivity(new Intent(getActivity(), mainActivity.getClass()));
    }

}