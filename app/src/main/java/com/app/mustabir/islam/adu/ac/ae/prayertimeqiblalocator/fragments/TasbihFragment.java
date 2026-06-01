package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments;

import android.app.Fragment;
import android.content.res.ColorStateList;
import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.DatabaseHelper;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.R;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models.TasbihCounter;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models.UserPreference;

import java.util.List;


public class TasbihFragment extends Fragment {

    private TextView tvDhikrName, tvCount, tvTarget, tvProgress;
    private Button btnCount, btnReset, btnSave;
    private Button btnDhikr1, btnDhikr2, btnDhikr3;
    private Button btnAddMore, btnRemove, btnConfirmDhikr;

    //visibally gone parts
    private LinearLayout layoutAddDhikr;
    private EditText etNewDhikr;

    private DatabaseHelper dbHelper;
    private List<TasbihCounter> tasbihList;
    private UserPreference globalSettings;
    private int currentIndex = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tasbih, container, false);

        //inatialize
        dbHelper = new DatabaseHelper(getActivity());
        globalSettings = dbHelper.getPreferences();

        // Bind Standard UI
        tvDhikrName  = (TextView) view.findViewById(R.id.tv_dhikr_name);
        tvCount      = (TextView) view.findViewById(R.id.tv_count);
        tvTarget     = (TextView) view.findViewById(R.id.tv_target);
        tvProgress   = (TextView) view.findViewById(R.id.tv_progress);
        btnCount     = (Button) view.findViewById(R.id.btn_count);
        btnReset     = (Button) view.findViewById(R.id.btn_reset);
        btnSave      = (Button) view.findViewById(R.id.btn_save);
        btnDhikr1    = (Button) view.findViewById(R.id.btn_dhikr1);
        btnDhikr2    = (Button) view.findViewById(R.id.btn_dhikr2);
        btnDhikr3    = (Button) view.findViewById(R.id.btn_dhikr3);


        // Bind Custom "Add More" UI
        btnAddMore      = (Button) view.findViewById(R.id.btn_dhikr_addMore);
        btnRemove       = (Button) view.findViewById(R.id.btn_dhikr_remove);
        layoutAddDhikr  = (LinearLayout) view.findViewById(R.id.layout_add_dhikr);
        etNewDhikr      = (EditText) view.findViewById(R.id.et_new_dhikr);
        btnConfirmDhikr = (Button) view.findViewById(R.id.btn_confirm_dhikr);

        loadData();
        setupListeners();

        return view ;
    }


    // DATA & UI LOADING

    private void loadData() {
        tasbihList = dbHelper.getAllTasbih();

        if (tasbihList.isEmpty()) return;

        // Set Dhikr Name
        if(tasbihList.size() >= 1) {
            btnDhikr1.setText(tasbihList.get(0).getDhikrName());
        }

        if(tasbihList.size() >= 2) {
            btnDhikr2.setText(tasbihList.get(1).getDhikrName());
        }

        if(tasbihList.size() >= 3) {
            btnDhikr3.setText(tasbihList.get(2).getDhikrName());
        }

        updateUI();

    }

    private void updateUI() {
        if (tasbihList == null || tasbihList.isEmpty()) return;

        TasbihCounter current = tasbihList.get(currentIndex);
        int target = globalSettings.getTasbihTarget();

        tvDhikrName.setText(current.getDhikrName());
        tvCount.setText(String.valueOf(current.getCurrentCount()));
        tvTarget.setText("Target: " + target);
        tvProgress.setText(current.getCurrentCount() + " / " + target);

        //  Reset ALL buttons to the "inactive" color (green_light)
        btnDhikr1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_light)));
        btnDhikr2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_light)));
        btnDhikr3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_light)));

        // Highlight ONLY the active button (green_primary)
        if (currentIndex == 0) {
            btnDhikr1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_primary)));
        } else if (currentIndex == 1) {
            btnDhikr2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_primary)));
        } else if (currentIndex == 2) {
            btnDhikr3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_primary)));
        }
       }

    // EVENT ROUTER (The "Table of Contents")

    private void setupListeners() {
         // Main Core Buttons

        //count
        btnCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCountClick();
        }
    });
     //reset
    btnReset.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            handleResetClick();
        }
    });
    //save
    btnSave.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            handleSaveClick();
        }
    });

            // Slot Selection Buttons
            // Dhikr 1
                btnDhikr1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleSelectSlot(0);
                }
                  });
                // Dhikr 2
                btnDhikr2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleSelectSlot(1);
                }
                 });
                  // Dhikr 3
                  btnDhikr3.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                    handleSelectSlot(2);
                   }
                   });

                    // Custom Editing Buttons
                    // Add More
                    btnAddMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            handleAddMoreClickUI();
                        }
                    });
                    // Remove
                    btnRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            handleRemoveClick();
                        }
                    });
                    // Confirm Dhikr
                    btnConfirmDhikr.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            handleConfirmDhikrClick();
                        }
                    });

            }



//  HELPER METHODS
    @SuppressWarnings("deprecation")
    private void handleCountClick() {
        if (tasbihList == null || tasbihList.isEmpty()) return;

        TasbihCounter current = tasbihList.get(currentIndex);
        int target = globalSettings.getTasbihTarget();

        if(current.getCurrentCount() < target) {
            current.setCurrentCount(current.getCurrentCount() + 1);
            updateUI();

            if (current.getCurrentCount() == target) {
                Toast.makeText(getActivity(), "Masha'Allah! Target reached! 🌙", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void handleResetClick() {
        if (tasbihList == null || tasbihList.isEmpty()) return;

        TasbihCounter current = tasbihList.get(currentIndex);
        current.setCurrentCount(0);
        dbHelper.resetTasbihCount(current.getId());
        updateUI();
        Toast.makeText(getActivity(), "Count reset", Toast.LENGTH_SHORT).show();

    }

    private void handleSaveClick() {
        if (tasbihList == null || tasbihList.isEmpty()) return;
        TasbihCounter current = tasbihList.get(currentIndex);

        dbHelper.updateTasbihCount(current.getId(), current.getCurrentCount());
        Toast.makeText(getActivity(), "Progress Saved ", Toast.LENGTH_SHORT).show();
    }

    private void handleSelectSlot(int index) {
        currentIndex = index;
        updateUI();
    }

    private void handleAddMoreClickUI() {
        if (layoutAddDhikr.getVisibility() == View.GONE) {
            layoutAddDhikr.setVisibility(View.VISIBLE);
            btnAddMore.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_primary)));

        } else {
            layoutAddDhikr.setVisibility(View.GONE);
            btnAddMore.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_light)));

        }
    }

    private void handleConfirmDhikrClick(){
        String newName = etNewDhikr.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(getActivity(), "Please type a name", Toast.LENGTH_SHORT).show();
            return;
        }

        TasbihCounter current = tasbihList.get(currentIndex);

        current.setDhikrName(newName);
        current.setCurrentCount(0);

        dbHelper.updateTasbihName(current.getId(), newName);
        dbHelper.resetTasbihCount(current.getId());

        if (currentIndex == 0) btnDhikr1.setText(newName);
        else if (currentIndex == 1) btnDhikr2.setText(newName);
        else if (currentIndex == 2) btnDhikr3.setText(newName);

        layoutAddDhikr.setVisibility(View.GONE);
        etNewDhikr.setText("");
        updateUI();

        Toast.makeText(getActivity(), "New Dhikr Saved!", Toast.LENGTH_SHORT).show();

    }

    private void handleRemoveClick(){
        if (tasbihList == null || tasbihList.isEmpty()) return;

        TasbihCounter current = tasbihList.get(currentIndex);
        String blankName = "Empty Slot";

        current.setDhikrName(blankName);
        current.setCurrentCount(0);

        dbHelper.updateTasbihName(current.getId(), blankName);
        dbHelper.resetTasbihCount(current.getId());

        if (currentIndex == 0) btnDhikr1.setText(blankName);
        else if (currentIndex == 1) btnDhikr2.setText(blankName);
        else if (currentIndex == 2) btnDhikr3.setText(blankName);

        updateUI();
        Toast.makeText(getActivity(), "Slot Cleared", Toast.LENGTH_SHORT).show();
    }


    // AUTO-SAVE ON EXIT
    @Override
    public void onPause() {
        super.onPause();
        if (tasbihList != null && !tasbihList.isEmpty()) {
            TasbihCounter current = tasbihList.get(currentIndex);
            dbHelper.updateTasbihCount(current.getId(), current.getCurrentCount());
        }
    }






    }
