package com.agos.ioextended2018;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.agos.ioextended2018.model.Item;
import com.androidquery.AQuery;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {

    private FirebaseUser user = null;
    private AQuery aquery = null;

    private FirebaseAnalytics firebaseAnalytics;

    private FirebaseFirestore fireStore;

    private FirebaseRemoteConfig firebaseRemoteConfig;

    private CollectionReference collectionReference;

    private List<Item> items = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();


        ((TextView) findViewById(R.id.profile_name)).setText(user.getDisplayName());

        aquery = new AQuery(this);
        aquery.id(R.id.profile_image).image(user.getPhotoUrl().toString());

        findViewById(R.id.close).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Main.this, Login.class));
            finish();
        });

        findViewById(R.id.picture).setOnClickListener(view -> startActivity(new Intent(Main.this, Picture.class)));

        //Firebase Remote Config
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        firebaseRemoteConfig.fetch(1).addOnCompleteListener(this, task -> {
            firebaseRemoteConfig.activateFetched();
            getSupportActionBar().setTitle(firebaseRemoteConfig.getString("app_title"));
        });

        //Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setCurrentScreen(Main.this, Main.class.getName(), null);

        //Firebase Cloud Messaging
        FirebaseMessaging.getInstance().subscribeToTopic("ioextended2018");

        //Firebase Firestore
        fireStore = FirebaseFirestore.getInstance();
        collectionReference = fireStore.collection("images");

        collectionReference.orderBy("date", Query.Direction.ASCENDING).addSnapshotListener((snapshots, e) -> {
            if (snapshots != null) {
                for (DocumentChange document : snapshots.getDocumentChanges()) {
                    if (document.getType().equals(DocumentChange.Type.ADDED)) {
                        items.add(0, document.getDocument().toObject(Item.class));
                    }
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });


        recyclerView = findViewById(R.id.list);

        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ItemAdapter(Main.this, items);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimaryLight);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            recyclerView.getAdapter().notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        });

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUser;
        public ImageView ivImage;
        public Item item;

        public ViewHolder(View v) {
            super(v);
            tvUser = v.findViewById(R.id.user);
            ivImage = v.findViewById(R.id.image);
        }
    }

    public class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<Item> items;
        private Context context;

        public ItemAdapter(Context context, List<Item> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Create a new View
            View v = LayoutInflater.from(this.context).inflate(R.layout.item_view, parent, false);
            ViewHolder vh = new ViewHolder(v);

            vh.ivImage.setOnClickListener(view -> {
                Intent intent = new Intent(Main.this, Image.class);

                intent.putExtra("item", vh.item);
                startActivity(intent);
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.item = items.get(position);
            holder.tvUser.setText(items.get(position).getUser());
            aquery.id(holder.ivImage).image("https://firebasestorage.googleapis.com/v0/b/ioextended-901ad.appspot.com/o/" + items.get(position).getUrl().replace("Pic", "thumbnail-Pic") + "?alt=media");
        }


        @Override
        public int getItemCount() {
            return this.items.size();
        }
    }
}
