// CCSA/app/src/main/java/com/gxuwz/ccsa/ui/resident/ContactPropertyActivity.java
package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;

public class ContactPropertyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_property);
        // 标题设置为"联系物业"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("联系物业");
        }
    }
}
