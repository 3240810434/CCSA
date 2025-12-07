package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.gxuwz.ccsa.R;

/**
 * 动态页面碎片（包含生活动态和邻里互助）
 */
public class DynamicFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 加载动态页面布局
        return inflater.inflate(R.layout.fragment_dynamic, container, false);
    }
}
