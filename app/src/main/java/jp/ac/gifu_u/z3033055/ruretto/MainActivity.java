package jp.ac.gifu_u.z3033055.ruretto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private EditText editTextStore;
    private Button btnAdd, btnRoulette;
    private ListView listView;
    private ArrayList<String> storeList;
    private ArrayAdapter<String> adapter;
    private Random random;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google マップのセットアップ
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 位置情報クライアントの初期化
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        // UI要素の取得
        editTextStore = findViewById(R.id.editTextStore);
        btnAdd = findViewById(R.id.btnAdd);
        btnRoulette = findViewById(R.id.btnRoulette);
        listView = findViewById(R.id.listView);

        // お店リストの初期化
        storeList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, storeList);
        listView.setAdapter(adapter);

        // ランダム生成用
        random = new Random();

        // お店を追加するボタン
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String storeName = editTextStore.getText().toString().trim();
                if (!storeName.isEmpty()) {
                    storeList.add(storeName);
                    adapter.notifyDataSetChanged();
                    editTextStore.setText("");  // 入力欄をクリア
                } else {
                    Toast.makeText(MainActivity.this, "お店の名前を入力してください", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ルーレット開始ボタン
        btnRoulette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!storeList.isEmpty()) {
                    int index = random.nextInt(storeList.size()); // ランダムに選択
                    String selectedStore = storeList.get(index);

                    Toast.makeText(MainActivity.this, "選ばれたお店: " + selectedStore, Toast.LENGTH_SHORT).show();

                    // Googleマップのナビを起動（現在地を考慮）
                    navigateToStore(selectedStore);
                } else {
                    Toast.makeText(MainActivity.this, "お店を追加してください", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 初期位置（東京駅）
        LatLng initialLocation = new LatLng(35.682839, 139.759455);
        mMap.addMarker(new MarkerOptions().position(initialLocation).title("東京駅"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));
    }

    // 現在地を取得
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Task<Location> task = fusedLocationClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        Toast.makeText(MainActivity.this, "現在地を取得しました", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Googleマップのナビを現在地から起動
    private void navigateToStore(String storeName) {
        if (currentLocation == null) {
            Toast.makeText(this, "現在地が取得できませんでした", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin="
                + currentLocation.latitude + "," + currentLocation.longitude
                + "&destination=" + Uri.encode(storeName)
                + "&travelmode=driving"); // 車移動の設定（徒歩なら "walking"）

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Googleマップが見つかりません", Toast.LENGTH_SHORT).show();
        }
    }

    // 位置情報の権限リクエストの結果を処理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "位置情報の許可が必要です", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
