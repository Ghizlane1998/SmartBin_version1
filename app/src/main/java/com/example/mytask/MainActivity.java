package com.example.mytask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mytask.dao.FirebaseHelper;
import com.example.mytask.fragments.EventsFragment;
import com.example.mytask.fragments.NotesFragment;
import com.example.mytask.fragments.ProjectFragment;
import com.example.mytask.fragments.TasksFragment;
import com.example.mytask.viewmodel.UserViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
//    FloatingActionButton addTaskBtn;
    ImageButton addTaskBtn;
//    RecyclerView recyclerView;
    ImageButton menuBtn;

    RelativeLayout userInfosLayout;

//    TaskAdapter taskAdapter;

    FirebaseUser firebaseUser;

    Uri uri;

    CircleImageView imageView;


    Button myListButton, eventsButton, projectsButton, notesButton;


    TextView usernameTextView;

    TextView weather;

    String weatherUrl;
    CardView projectCard,eventCard,notesCard,tasksCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ///// start Cards //////
        // Récupérer les références des CardViews
         tasksCard = findViewById(R.id.TasksCard);
         notesCard = findViewById(R.id.NotesCard);
         eventCard = findViewById(R.id.EventCard);
         projectCard = findViewById(R.id.ProjetCard);

        // Définir les listeners de clic pour chaque CardView
        tasksCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Traitement lors du clic sur la carte "Tasks"
                Toast.makeText(MainActivity.this, "Clicked Tasks Card", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this,BinActivity.class));
            }
        });

        notesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Traitement lors du clic sur la carte "Notes"
                Toast.makeText(MainActivity.this, "Clicked Notes Card", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this,TriActivity.class));
            }
        });

        eventCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Traitement lors du clic sur la carte "Events"
                Toast.makeText(MainActivity.this, "Clicked Events Card", Toast.LENGTH_SHORT).show();
            }
        });

        projectCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Traitement lors du clic sur la carte "Projects"
                Toast.makeText(MainActivity.this, "Clicked Projects Card", Toast.LENGTH_SHORT).show();
            }
        });
///// End Cards //////
        usernameTextView = findViewById(R.id.username_text_view);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUsername().observe(this, username -> {
            if (username != null) {
                usernameTextView.setText(username);
            } else {
                usernameTextView.setText("Username not available");
            }
        });

        imageView = findViewById(R.id.profile_image);
        menuBtn = findViewById(R.id.menu_btn);
        userInfosLayout = findViewById(R.id.user_info);
        weather = findViewById(R.id.weather);
        this.obtainLocation();



        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        uri = firebaseUser.getPhotoUrl();
        //image view set image uri should not be used with regular URIs. So we are using Picasso

        if(uri != null){
            Picasso.get().load(uri).into(imageView);
        }

        menuBtn.setOnClickListener(v -> showMenu());
        userInfosLayout.setOnClickListener( v -> {
            startActivity(new Intent(MainActivity.this,ProfileActivity.class));
        });

//        setupRecyclerView();

        // Find the TasksFragment using FragmentManager
//        TasksFragment tasksFragment = (TasksFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);

    }
// End of OnCreate //
    void showMenu() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this,menuBtn);
        popupMenu.getMenu().add("Logout");
        popupMenu.getMenu().add("Profile");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getTitle() == "Logout"){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this,SignInActivity.class));
                    finish();
                    return true;
                }
                if(menuItem.getTitle() == "Profile"){
                    startActivity(new Intent(MainActivity.this,ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

//    void setupRecyclerView(){
//        Query query = Utility.getCollectionReferenceForTask().orderBy("timestamp",Query.Direction.DESCENDING);
//        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
//                .setQuery(query,Task.class).build();
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        taskAdapter = new TaskAdapter(options,this);
//        recyclerView.setAdapter(taskAdapter);
//    }


//    @Override
//    protected void onStart() {
//        super.onStart();
////        taskAdapter.startListening();
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
////        taskAdapter.stopListening();
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
////        taskAdapter.notifyDataSetChanged();
//    }


    private void getWeatherData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d("Weather", "entred");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, weatherUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse JSON response
                            JSONObject obj = new JSONObject(response);
                            JSONArray arr = obj.getJSONArray("data");
                            JSONObject obj2 = arr.getJSONObject(0);

                            // Extract weather data
                            String temp = obj2.getString("temp") + " °C " ;
//                            String precipitation = obj2.getString("precip") + " mm";
//                            String wind = obj2.getString("wind_spd") + " m/s " + obj2.getString("wind_cdir");
//                            String city = obj2.getString("city_name");


                            Log.d("Weather", temp.toString());

                            // Update UI
                            runOnUiThread(() -> {
                                weather.setText(temp);
//                                weather.setText(city);
//                                weather.setText(precipitation);
//                                weather.setText(wind);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }


    private void obtainLocation() {
        weatherUrl = "http://api.weatherbit.io/v2.0/current?" +
                "lat=" + "34.011036495662474" +
                "&lon=" +  "-6.849026873799245" +
                "&key=" + "089f022ca905454b9c61e4196e8773a6";

        // This function will fetch data from URL

        Log.d("Weather Url", weatherUrl.toString());
        getWeatherData();
        // });
    }
}