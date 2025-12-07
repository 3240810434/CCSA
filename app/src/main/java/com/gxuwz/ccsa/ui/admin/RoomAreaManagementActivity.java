package com.gxuwz.ccsa.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.RoomAreaAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.RoomArea;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomAreaManagementActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "RoomAreaManagement";
    private String community;
    private Spinner spinnerBuilding;
    private RecyclerView recyclerViewRooms;
    private Button btnSave;
    private TextView tvCommunityName;
    private RoomAreaAdapter adapter;
    private List<String> buildingList = new ArrayList<>();
    private List<RoomArea> currentRoomAreas = new ArrayList<>();
    private static final int DEFAULT_BUILDING_COUNT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_area_management);

        community = getIntent().getStringExtra("community");
        if (community == null || community.trim().isEmpty()) {
            Toast.makeText(this, "未获取到小区信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadBuildings();
    }

    private void initViews() {
        tvCommunityName = findViewById(R.id.tv_community_name);
        tvCommunityName.setText(community + " 房屋面积信息");

        spinnerBuilding = findViewById(R.id.spinner_building);
        recyclerViewRooms = findViewById(R.id.recycler_view_rooms);
        recyclerViewRooms.setLayoutManager(new LinearLayoutManager(this));

        btnSave = findViewById(R.id.btn_save);
    }

    private void setupListeners() {
        spinnerBuilding.setOnItemSelectedListener(this);
        btnSave.setOnClickListener(v -> saveRoomAreas());
    }

    private void loadBuildings() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<String> buildings = db.roomAreaDao().getBuildingsByCommunity(community);
            Log.d(TAG, "查询到" + buildings.size() + "个楼栋（" + community + "）");

            if (buildings.isEmpty()) {
                Log.d(TAG, "无楼栋数据，添加默认楼栋");
                List<RoomArea> defaultRooms = new ArrayList<>();
                for (int i = 1; i <= DEFAULT_BUILDING_COUNT; i++) {
                    String building = i + "栋";
                    buildings.add(building);
                    // 生成房间号时确保楼层正确排序
                    for (int floor = 1; floor <= 10; floor++) {  // 明确使用floor变量表示楼层
                        for (int room = 1; room <= 2; room++) {
                            String roomNumber = floor + String.format("%02d", room);
                            defaultRooms.add(new RoomArea(community, building, roomNumber, 100.0));
                        }
                    }
                }
                db.roomAreaDao().insert(defaultRooms.toArray(new RoomArea[0]));
            } else {
                // 对已有楼栋进行排序（按数字顺序）
                Collections.sort(buildings, (b1, b2) -> {
                    int num1 = Integer.parseInt(b1.replace("栋", ""));
                    int num2 = Integer.parseInt(b2.replace("栋", ""));
                    return Integer.compare(num1, num2);
                });
            }

            buildingList.addAll(buildings);

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, buildingList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBuilding.setAdapter(adapter);
                // 设置默认选中第一栋
                if (!buildingList.isEmpty()) {
                    spinnerBuilding.setSelection(0);
                }
            });
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final String selectedBuilding = buildingList.get(position);
        loadRoomAreas(selectedBuilding);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void loadRoomAreas(String building) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<RoomArea> rooms = db.roomAreaDao().getByCommunityAndBuilding(community, building);
            Log.d(TAG, "查询到" + rooms.size() + "条数据（" + community + "-" + building + "）");

            if (rooms.isEmpty()) {
                Log.d(TAG, "数据库无该楼栋数据，创建默认数据");
                for (int floor = 1; floor <= 10; floor++) {  // 明确使用floor变量表示楼层
                    for (int room = 1; room <= 2; room++) {
                        String roomNumber = floor + String.format("%02d", room);
                        rooms.add(new RoomArea(community, building, roomNumber, 100.0));
                    }
                }
            } else {
                // 对房间号按楼层进行排序
                Collections.sort(rooms, (r1, r2) -> {
                    // 提取楼层号进行比较
                    int floor1 = Integer.parseInt(r1.getRoomNumber().substring(0, r1.getRoomNumber().length() - 2));
                    int floor2 = Integer.parseInt(r2.getRoomNumber().substring(0, r2.getRoomNumber().length() - 2));
                    return Integer.compare(floor1, floor2);
                });
            }

            currentRoomAreas = rooms;

            runOnUiThread(() -> {
                adapter = new RoomAreaAdapter(this, currentRoomAreas, community);
                recyclerViewRooms.setAdapter(adapter);
            });
        });
    }

    private void saveRoomAreas() {
        if (adapter == null) return;

        // 关键修复：保存前强制更新所有输入框的值到数据模型
        adapter.updateAllRoomAreas();

        List<RoomArea> roomAreas = adapter.getRoomAreas();
        if (roomAreas.isEmpty()) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            try {
                db.runInTransaction(() -> {
                    String building = roomAreas.get(0).getBuilding();
                    db.roomAreaDao().deleteByCommunityAndBuilding(community, building);
                    db.roomAreaDao().insert(roomAreas.toArray(new RoomArea[0]));
                });

                List<RoomArea> savedRooms = db.roomAreaDao()
                        .getByCommunityAndBuilding(community, roomAreas.get(0).getBuilding());
                Log.d(TAG, "保存成功，查询到" + savedRooms.size() + "条数据");

                runOnUiThread(() -> {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    currentRoomAreas = savedRooms;
                    adapter = new RoomAreaAdapter(this, currentRoomAreas, community);
                    recyclerViewRooms.setAdapter(adapter);
                });
            } catch (Exception e) {
                Log.e(TAG, "保存失败：" + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}