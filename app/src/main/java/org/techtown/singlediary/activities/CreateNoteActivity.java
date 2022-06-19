package org.techtown.singlediary.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.common.base.MoreObjects;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.techtown.singlediary.R;
import org.techtown.singlediary.database.NotesDatabase;
import org.techtown.singlediary.entities.Note;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateNoteActivity extends AppCompatActivity {

        private String id;

        private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
        private TextView textDateTime;
        private View viewSubtitleIndicator;
        private ImageView imageNote;
        private TextView textWebURL;
        private LinearLayout layoutWebURL;

        private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
        private static final int REQUEST_CODE_SELECT_IMAGE = 2;

        private AlertDialog dialogAddURL;
        private AlertDialog dialogDeleteNote;

        public Uri selectedImageUri;

        private String selectedNoteColor;
        private String selectedImagePath;
        private Note alreadyAvailableNote;

        private String[] names = {"보기1","보기2","보기3","보기4","보기5"};


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_note);

            ImageView imageAddNoteMain = findViewById(R.id.imageBack);
            imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            inputNoteTitle = findViewById(R.id.inputNoteTitle);
            inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
            inputNoteText = findViewById(R.id.inputNote);
            textDateTime = findViewById(R.id.textDateTime);
            imageNote = findViewById(R.id.imageNote);
            textWebURL = findViewById(R.id.textWebURL);
            layoutWebURL = findViewById(R.id.layoutWebURL);


            viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);

            textDateTime.setText(
                    new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                            .format(new Date())
            );

            ImageView imageSave = findViewById(R.id.imageSave);
            imageSave.setOnClickListener(v -> saveNote());

            selectedNoteColor = "#333333";
            selectedImagePath = "";

            if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
                alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
                setViewOrUpdateNote();
            }

            findViewById(R.id.imageRemoveWebURL).setOnClickListener(v -> {
                textWebURL.setText(null);
                layoutWebURL.setVisibility(View.GONE);
            });

            findViewById(R.id.imageRemoveImage).setOnClickListener(v -> {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                selectedImagePath = "";
            });

          /*  if (getIntent().getBooleanExtra("isFromQuickActions", false)) {
                String type = getIntent().getStringExtra("quickActionType");
                if (type != null) {
                    if (type.equals("image")) {
                        selectedImagePath = getIntent().getStringExtra("imagePath");
                        imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                    } else if (type.equals("URL")) {
                        textWebURL.setText(getIntent().getStringExtra("URL"));
                        layoutWebURL.setVisibility(View.VISIBLE);
                    }
                }
            }*/

            initMiscellaneous();
            setSubtitleIndicatorColor();

            // 스피너 관련 코드
            Spinner spinner = findViewById(R.id.spinner);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item,names);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setSelection(getIndex(spinner, alreadyAvailableNote.getSubtitle()));

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    ((TextView)adapterView.getChildAt(0)).setTextColor(Color.WHITE);
                    inputNoteSubtitle.setText(names[i]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    inputNoteSubtitle.setText("");
                }
            });
            // 스피너 코드 끝

        }

    //스피너 관련 코드
    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            Toast.makeText(this,"들어감",Toast.LENGTH_SHORT).show();
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return 0;
    }
    // 스피너 관련 끝

        private void setViewOrUpdateNote() {
            inputNoteTitle.setText(alreadyAvailableNote.getTitle());
            inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
            inputNoteText.setText(alreadyAvailableNote.getNoteText());
            textDateTime.setText(alreadyAvailableNote.getDateTime());


            final String imagePathStr = alreadyAvailableNote.getImagePath();
            if (imagePathStr != null && !imagePathStr.trim().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(imagePathStr));
                imageNote.setVisibility(View.VISIBLE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                selectedImagePath = imagePathStr;
            }

            final String webLinkStr = alreadyAvailableNote.getWebLink();
            if (webLinkStr != null && !webLinkStr.trim().isEmpty()) {
                textWebURL.setText(alreadyAvailableNote.getWebLink());
                layoutWebURL.setVisibility(View.VISIBLE);
            }
        }

        private void saveNote() {
            if (inputNoteTitle.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
                return;
            } else if (inputNoteSubtitle.getText().toString().trim().isEmpty()
                    && inputNoteText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            final Note note = new Note();
            note.setTitle(inputNoteTitle.getText().toString());
            note.setSubtitle(inputNoteSubtitle.getText().toString());
            note.setNoteText(inputNoteText.getText().toString());
            note.setDateTime(textDateTime.getText().toString());
            note.setColor(selectedNoteColor);
            note.setImagePath(selectedImagePath);

            if (layoutWebURL.getVisibility() == View.VISIBLE) {
                note.setWebLink(textWebURL.getText().toString());
            }

            //id = mStore.collection("note").document().getId();
            if (alreadyAvailableNote != null) {
                note.setId(alreadyAvailableNote.getId());

            }

            // 파이어베이스 이미지 업로드ㅡ
            if(selectedImageUri != null) {
                FirebaseStorage storage = FirebaseStorage.getInstance(); // 스토리지 인스턴스 만들고
                StorageReference storageRef = storage.getReference(); // 스토리지 참조
                // 파일명 만들기
                String filename = "test1.jpg";
                Uri file_fimg = selectedImageUri;
                Log.d("유알", String.valueOf(file_fimg));
                StorageReference riversRef = storageRef.child("test_image/" + filename);
                UploadTask uploadTask = riversRef.putFile(file_fimg);
                // 새로운 이미지 저장
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "이미지 업로드", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @SuppressLint("StaticFieldLeak")
            class SaveNoteTask extends AsyncTask<Void, Void, Void> {
                @Override
                protected Void doInBackground(Void... voids) {
                    NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().insertNote(note);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            new SaveNoteTask().execute();

        }

        private void initMiscellaneous() {
            final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
            final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
            layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(v -> {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });
            final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
            final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
            final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
            final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
            final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);

            layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(v -> {
                selectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            });

            layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(v -> {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            });

            layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(v -> {
                selectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            });

            layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(v -> {
                selectedNoteColor = "#3A52FC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            });

            layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(v -> {
                selectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();
            });

            if (alreadyAvailableNote != null) {
                final String noteColorCode = alreadyAvailableNote.getColor();
                if (noteColorCode != null && !noteColorCode.trim().isEmpty()) {
                    switch (noteColorCode) {
                        case "#FDBE3B":
                            layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                            break;
                        case "#FF4842":
                            layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                            break;
                        case "#3A52FC":
                            layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                            break;
                        case "#000000":
                            layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                            break;
                    }
                }
            }

            layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            });

            layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            });

            if (alreadyAvailableNote != null) {
                layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
                layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(v -> {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                });
            }
        }

        private void showDeleteNoteDialog() {
            if (dialogDeleteNote == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
                View view = LayoutInflater.from(this).inflate(
                        R.layout.layout_delete_note,
                        (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
                );
                builder.setView(view);
                dialogDeleteNote = builder.create();
                if (dialogDeleteNote.getWindow() != null) {
                    dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                view.findViewById(R.id.textDeleteNote).setOnClickListener(v -> {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);

                            dialogDeleteNote.dismiss();
                            finish();
                        }
                    }

                    new DeleteNoteTask().execute();
                });

                view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDeleteNote.dismiss());
            }

            dialogDeleteNote.show();
        }

        private void setSubtitleIndicatorColor() {
            GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
            gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
        }

        @SuppressLint("QueryPermissionsNeeded")
        private void selectImage() {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
                if (data != null) {
                    selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        try {
                             InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                             Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                             imageNote.setImageBitmap(bitmap);
                             imageNote.setVisibility(View.VISIBLE);
                             findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                            selectedImagePath = getPathFromUri(selectedImageUri);



                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        private String getPathFromUri(Uri contentUri) {
            String filePath;
            Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
            if (cursor == null) {
                filePath = contentUri.getPath();
            } else {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex("_data");
                filePath = cursor.getString(index);
                cursor.close();
            }
            return filePath;
        }

        private void showAddURLDialog() {
            if (dialogAddURL == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
                View view = LayoutInflater.from(this)
                        .inflate(R.layout.layout_add_url, findViewById(R.id.layoutAddUrlContainer));
                builder.setView(view);

                dialogAddURL = builder.create();
                if (dialogAddURL.getWindow() != null) {
                    dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                final EditText inputURL = view.findViewById(R.id.inputURL);
                inputURL.requestFocus();

                view.findViewById(R.id.textAdd).setOnClickListener(v -> {
                    final String inputURLStr = inputURL.getText().toString().trim();

                    if (inputURLStr.isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURLStr).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        textWebURL.setText(inputURL.getText().toString());
                        layoutWebURL.setVisibility(View.VISIBLE);
                        dialogAddURL.dismiss();
                    }
                });

                view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogAddURL.dismiss());
            }
            dialogAddURL.show();
        }

}