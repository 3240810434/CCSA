package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.gxuwz.ccsa.R;

/**
 * 邻里互助子页面
 */
public class NeighborHelpFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neighbor_help, container, false);

        // TODO: 初始化 RecyclerView 和 Adapter 加载邻里互助数据

        return view;
    }
}