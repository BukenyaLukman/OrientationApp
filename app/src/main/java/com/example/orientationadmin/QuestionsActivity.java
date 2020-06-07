package com.example.orientationadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestionsActivity extends AppCompatActivity {
    private Button add,excel;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private QuestionsAdapter adapter;
    public static List<QuestionModel> list;
    private ProgressDialog loadingBar;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);


        myRef = FirebaseDatabase.getInstance().getReference();


        toolbar = findViewById(R.id.toolbar_questions);
        setSupportActionBar(toolbar);
        final String CategoryName = getIntent().getStringExtra("category");
        getSupportActionBar().setTitle(CategoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);

        add = findViewById(R.id.add_btn);
        excel = findViewById(R.id.excel_btn);
        recyclerView = findViewById(R.id.question_recyclerView);



        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();
        adapter = new QuestionsAdapter(list, CategoryName, new QuestionsAdapter.DeleteListener() {
            @Override
            public void onLongClick(final int position, final String Id) {

                new AlertDialog.Builder(QuestionsActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Question")
                        .setMessage("Are you sure you want to delete this Question ?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingBar.setTitle("Deleting Question");
                                loadingBar.setMessage("Please wait...");
                                loadingBar.setCanceledOnTouchOutside(false);
                                loadingBar.show();
                                myRef.child("SETS").child(CategoryName).child("questions").child(Id).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    list.remove(position);
                                                    adapter.notifyItemRemoved(position);
                                                    loadingBar.dismiss();
                                                }else{
                                                    String message = task.getException().toString();
                                                    Toast.makeText(QuestionsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
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
        getData(CategoryName);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addQuestion = new Intent(QuestionsActivity.this,AddQuestionActivity.class);
                addQuestion.putExtra("categoryName",CategoryName);
                startActivity(addQuestion);
            }
        });

        excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(QuestionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    selectFile();
                }else{
                    ActivityCompat.requestPermissions(QuestionsActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);

                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED){

            }else{
                Toast.makeText(this, "Please Grant Permissions!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);

    }

    private void selectFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"),102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 102){
            if(resultCode == RESULT_OK){
                String filePath = data.getData().getPath();
                if(filePath.endsWith("xlsx")){
                    Toast.makeText(this, "File Selected", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Please choose an Excel File", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    private void getData(String CategoryName){
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

            myRef.child("SETS").child(CategoryName)
                .child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Fixing the order by Later
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    String id = dataSnapshot1.getKey();
                    String question = dataSnapshot1.child("question").getValue().toString();
                    String A = dataSnapshot1.child("optionA").getValue().toString();
                    String B = dataSnapshot1.child("optionB").getValue().toString();
                    String C = dataSnapshot1.child("optionC").getValue().toString();
                    String D = dataSnapshot1.child("optionD").getValue().toString();
                    String correctAns = dataSnapshot1.child("correctAns").getValue().toString();

                    list.add(new QuestionModel(id,question,A,B,C,D,correctAns));
                }
                loadingBar.dismiss();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }
}
