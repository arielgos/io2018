package com.agos.ioextended2018;

import android.app.Application;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.agos.ioextended2018.model.Item;
import com.agos.ioextended2018.util.CameraSource;
import com.agos.ioextended2018.util.CameraSourcePreview;
import com.agos.ioextended2018.util.GraphicOverlay;
import com.agos.ioextended2018.util.ImageLabelingProcessor;
import com.agos.ioextended2018.util.LabelGraphic;
import com.agos.ioextended2018.util.PhotoHandler;
import com.androidquery.util.Progress;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Picture extends AppCompatActivity {


    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference images;
    private Bitmap bitmap;

    private Item item;

    private FirebaseFirestore fireStore;
    private CollectionReference collectionReference;

    private FirebaseUser user = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        getSupportActionBar().hide();

        user = FirebaseAuth.getInstance().getCurrentUser();


        preview = findViewById(R.id.firePreview);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);

        createCameraSource();

        findViewById(R.id.picture).setOnClickListener(view -> {

            progressDialog = ProgressDialog.show(this, null, getString(R.string.sending), true);

            item = new Item();
            item.setLabels(getLabels());
            item.setDate(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
            item.setUser(user.getDisplayName());
            item.setUserImage(user.getPhotoUrl().toString());

            //generamos la imagen
            cameraSource.takePicture(getApplicationContext(), () -> {

                preview.stop();

                String fileName = PhotoHandler.getFileName();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFile(fileName, options);

                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();

                Task<FirebaseVisionText> result = detector.detectInImage(image)
                        .addOnSuccessListener(firebaseVisionText -> {
                            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                                String text = block.getText();
                                item.setText(text);
                            }


                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            byte[] data = baos.toByteArray();

                            String newName = "Pic_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".jpg";

                            item.setUrl(newName);


                            StorageReference imageReference = images.child(newName);
                            UploadTask uploadTask = imageReference.putBytes(data);
                            uploadTask.addOnFailureListener(e -> {
                                e.printStackTrace();
                                Crashlytics.log(Log.ERROR, App.tag, e.getMessage());
                                Crashlytics.logException(e);
                            }).addOnSuccessListener(taskSnapshot -> {
                                progressDialog.dismiss();
                                collectionReference.add(item);
                                finish();
                            });

                        })
                        .addOnFailureListener(
                                e -> {
                                    progressDialog.dismiss();
                                    e.printStackTrace();
                                    Crashlytics.log(Log.ERROR, App.tag, e.getMessage());
                                    Crashlytics.logException(e);
                                });
            });


        });

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        images = storageReference.child("images");

        fireStore = FirebaseFirestore.getInstance();
        collectionReference = fireStore.collection("images");
    }

    private String getLabels() {
        List<String> items = new ArrayList<>();
        for (GraphicOverlay.Graphic graphic : graphicOverlay.getGraphics()) {
            List<FirebaseVisionLabel> labels = ((LabelGraphic) graphic).getLabels();
            for (FirebaseVisionLabel label : labels) {
                items.add(label.getLabel());
            }
        }
        return TextUtils.join(",", items);
    }

    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        cameraSource.setMachineLearningFrameProcessor(new ImageLabelingProcessor());
        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                cameraSource.release();
                cameraSource = null;
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}
