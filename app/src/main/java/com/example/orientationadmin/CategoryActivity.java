package com.example.orientationadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
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



import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class CategoryActivity extends AppCompatActivity {

    private Dialog CategoryDialog;
    private CircleImageView addImage;
    private EditText CategoryName,NumberOfQuestions;
    private Button addBtn;
    private static final int GalleryPick = 1;
    private String CategoryID = "";



    private Uri ImageUri;
    List<CategoryModel> list;
    private CategoryAdapter adapter;

    private FirebaseUser currentUser;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    private String downloadUrl;
    private String CategoryNameInput;
    private Integer Numbers;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference myRef;
    private StorageReference CategoryImagesRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);


        CategoryID = getIntent().getStringExtra("categoryID");

        myRef = FirebaseDatabase.getInstance().getReference();
        CategoryImagesRef = FirebaseStorage.getInstance().getReference().child("categories");

        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();



        toolbar = findViewById(R.id.category_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");


        recyclerView = findViewById(R.id.categories_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();
        adapter = new CategoryAdapter(list, new CategoryAdapter.DeleteListener() {
            @Override
            public void onDelete(final String key, final int position) {

                new AlertDialog.Builder(CategoryActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Category")
                        .setMessage("Are you sure you want to delete this category ?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingBar.setTitle("Deleting Category");
                                loadingBar.setMessage("Please wait...");
                                loadingBar.setCanceledOnTouchOutside(false);
                                loadingBar.show();
                                myRef.child("Categories").child(key).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    list.remove(position);
                                                    adapter.notifyItemRemoved(position);
                                                    loadingBar.dismiss();
                                                }else{
                                                    String message = task.getException().toString();
                                                    Toast.makeText(CategoryActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();

                                                }
                                        }
                                 });
                            }
                        }).setNegativeButton("Cancel",null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        recyclerView.setAdapter(adapter);

        CategoryDialog = new Dialog(this);
        CategoryDialog.setContentView(R.layout.add_category_dialog);
        CategoryDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        CategoryDialog.setCancelable(true);


        addImage = CategoryDialog.findViewById(R.id.category_image);
        addBtn = CategoryDialog.findViewById(R.id.category_add);
        CategoryName = CategoryDialog.findViewById(R.id.category_name);
        NumberOfQuestions = CategoryDialog.findViewById(R.id.category_questions_expected);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateCategoryData();
            }
        });

        myRef.child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    list.add(dataSnapshot1.getValue(CategoryModel.class));

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CategoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateCategoryData() {
        CategoryNameInput = CategoryName.getText().toString();
        Numbers = Integer.parseInt(NumberOfQuestions.getText().toString());
        for(CategoryModel model : list){
            if(CategoryNameInput.equals(model.getName())){
                Toast.makeText(CategoryActivity.this, "Category Already Exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if(ImageUri == null){
            Toast.makeText(this, "Category Image is required", Toast.LENGTH_SHORT).show();
        }else if(NumberOfQuestions.getText().toString().trim().isEmpty() || Numbers.equals("")){
            Toast.makeText(this, "Expected Numbers Of Questions missing", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(CategoryNameInput)){
            Toast.makeText(this, "Category Name is Empty", Toast.LENGTH_SHORT).show();
        }else{
            uploadCategory();
        }
    }

    private void uploadCategory() {
        loadingBar.setTitle("Uploading Category");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        final StorageReference filePath = CategoryImagesRef.child(ImageUri.getLastPathSegment() + ".jpg");
        final UploadTask uploadTask = filePath.putFile(ImageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(CategoryActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(CategoryActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            downloadUrl = task.getResult().toString();
                            loadingBar.dismiss();
                            Toast.makeText(CategoryActivity.this, "Category Image obtained Successfully..", Toast.LENGTH_SHORT).show();
                            saveCategoryInfoToDatabase();
                        }
                    }
                });
            }
        });
    }

    private void saveCategoryInfoToDatabase() {
        HashMap<String,Object> catMap = new HashMap<>();
        catMap.put("name",CategoryNameInput);
        catMap.put("url",downloadUrl);
        catMap.put("sets",Numbers);

        myRef.child("Categories").child(CategoryNameInput).updateChildren(catMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful()){
                           CategoryDialog.dismiss();
                           loadingBar.dismiss();
                           Toast.makeText(CategoryActivity.this, "Category is added Successfully", Toast.LENGTH_SHORT).show();
                       }else{
                           loadingBar.dismiss();
                           String message = task.getException().toString();
                           Toast.makeText(CategoryActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                       }
                    }
                });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GalleryPick && resultCode ==RESULT_OK && data != null){
            ImageUri = data.getData();
            addImage.setImageURI(ImageUri);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() ==R.id.add){
            ///Dialog show
            CategoryDialog.show();

            //finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
