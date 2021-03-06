package com.hackaholic.trainpanda.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.GsonBuilder;
import com.hackaholic.trainpanda.Cells.CategoryCell;
import com.hackaholic.trainpanda.Cells.CheckType;
import com.hackaholic.trainpanda.Cells.MenuItemCell;
import com.hackaholic.trainpanda.Cells.SubCategoryCell;
import com.hackaholic.trainpanda.R;
import com.hackaholic.trainpanda.adapter.ListAdapter;
import com.hackaholic.trainpanda.adapter.RestrauntMenuAdapter;
import com.hackaholic.trainpanda.custom.ComplexPreferences;
import com.hackaholic.trainpanda.helpers.API;
import com.hackaholic.trainpanda.helpers.EnumType;
import com.hackaholic.trainpanda.helpers.GetPostClass;
import com.hackaholic.trainpanda.ui.fragments.BookHotel;
import com.hackaholic.trainpanda.ui.fragments.FareEnquiry;
import com.hackaholic.trainpanda.ui.fragments.MainFragment;
import com.hackaholic.trainpanda.ui.fragments.MyFoodOrder;
import com.hackaholic.trainpanda.ui.fragments.MySearchedPNR;
import com.hackaholic.trainpanda.ui.fragments.OrderFood;
import com.hackaholic.trainpanda.ui.fragments.PNRFragment;
import com.hackaholic.trainpanda.ui.fragments.RunningStatus;
import com.hackaholic.trainpanda.ui.fragments.SearchTrain;
import com.hackaholic.trainpanda.ui.fragments.SeatAvailability;
import com.hackaholic.trainpanda.ui.fragments.StationInfo;
import com.hackaholic.trainpanda.ui.fragments.TrainArraiving;
import com.hackaholic.trainpanda.utility.ExpandableTextView;
import com.hackaholic.trainpanda.utils.SlidingMenuLayout;
import com.squareup.picasso.Picasso;

import Model.Categories;
import Model.MenuItems;
import Model.Restraunt;
import Model.SubCategories;
import Model.TopCategories;
import Model.TopMenuItems;
import Model.TopSubCategories;
import de.hdodenhof.circleimageview.CircleImageView;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

public class RestrauntDetail extends FragmentActivity {
    TextView txtHotelName, menu, time, minOrder, delivery, tv_restaurant_timings, tv_restaurant_mobile, tv_hotel_kunal_place_order;
    LinearLayout nonveg, veg;
    int listPosition;
    ProgressDialog progressDialog1, progressDialog2, progressDialog3;
    Restraunt valuesRestraunt;
    ListView listviewMenu;
    LinearLayout ListItemLinear;
    TopCategories catg;
    TopSubCategories subCatg;
    TopMenuItems menuItems;
    int counter = 0;
    RestrauntMenuAdapter resAdapter;
    ArrayList<CheckType> checkTypeList;
    ImageView imgBack, imgRest;
    ExpandableTextView txtDesc;
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    public static HashMap<MenuItems, Integer> selectedItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_detail);
        initViews();
        selectedItems = new HashMap<>();
        listPosition = getIntent().getIntExtra("pos", 0);

        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(RestrauntDetail.this, "user_pref", 0);
        valuesRestraunt = complexPreferences.getObject("current_restraunt", Restraunt.class);


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        try {

            txtDesc.setText(valuesRestraunt.Restraunt.get(listPosition).text);

            if (valuesRestraunt.Restraunt.get(listPosition).images.size() != 0) {
                Glide.with(RestrauntDetail.this).load(API.BASE_IMAGE_URL + valuesRestraunt.Restraunt.get(listPosition).images.get(0).url).thumbnail(0.1f).into(imgRest);
            }
        } catch (Exception e) {
            Log.e("exc null images", e.toString());
        }


        callWebServices();

        tv_hotel_kunal_place_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(selectedItems.size()==0){
                    Toast.makeText(RestrauntDetail.this, "Please select any item to place order", Toast.LENGTH_SHORT).show();
                }

                else{
                        Intent i = new Intent(RestrauntDetail.this, PlaceOrder.class);
                        startActivity(i);
                    }
            }
        });


    }

    void initViews() {

        imgRest = (ImageView) findViewById(R.id.imgRest);
        txtDesc = (ExpandableTextView) findViewById(R.id.txtDesc);
        imgBack = (ImageView) findViewById(R.id.imgBack);
        tv_hotel_kunal_place_order = (TextView) findViewById(R.id.tv_hotel_kunal_place_order);
        ListItemLinear = (LinearLayout) findViewById(R.id.ListItemLinear);
        //listviewMenu = (ListView)findViewById(R.id.listviewMenu);

        txtHotelName = (TextView) findViewById(R.id.txtHotelName);
        tv_restaurant_mobile = (TextView) findViewById(R.id.mobile);
        menu = (TextView) findViewById(R.id.menu);
        time = (TextView) findViewById(R.id.time);

        minOrder = (TextView) findViewById(R.id.minOrder);
        delivery = (TextView) findViewById(R.id.delivery);
        veg = (LinearLayout) findViewById(R.id.ll_veg_boundry);
        nonveg = (LinearLayout) findViewById(R.id.ll_nonveg_boundry);
    }

    void callWebServices() {
        String MainCategory = API.BASE_URL + "categories";
        String SubCategory = API.BASE_URL + "subcategories";
        String MenuItemLink = API.BASE_URL + "items?filter[where][restaurantId]=" + valuesRestraunt.Restraunt.get(listPosition).id;

        progressDialog1 = new ProgressDialog(RestrauntDetail.this);
        progressDialog2 = new ProgressDialog(RestrauntDetail.this);
        progressDialog3 = new ProgressDialog(RestrauntDetail.this);

        processFetchDetails(MainCategory, progressDialog1, 1);
        processFetchDetails(SubCategory, progressDialog2, 2);
        processFetchDetails(MenuItemLink, progressDialog3, 3);

        fillupdetails();


    }

    private void setAdapter() {
        LayoutInflater inflater = getLayoutInflater();

        for (Categories categories : catg.Categories) {
            fillCategory(categories);
        }

/*        for (int i = 0; i < catg.Categories.size(); i++) {
            View mainCatg = inflater.inflate(R.layout.rest_catg_item, ListItemLinear, false);
            //    ListItemLinear.addView(mainCatg);
            TextView title = (TextView) mainCatg.findViewById(R.id.title);
            title.setText(catg.Categories.get(i).name);

            for (int j = 0; j < subCatg.SubCategories.size(); j++) {

                if (subCatg.SubCategories.get(j).categoryId.equalsIgnoreCase(catg.Categories.get(i).id)) {

                    View subCatg2 = inflater.inflate(R.layout.rest_subcatg_item, ListItemLinear, false);

                    //      ListItemLinear.addView(subCatg2);


                    TextView title2 = (TextView) subCatg2.findViewById(R.id.subtitle);
                    title2.setText(subCatg.SubCategories.get(j).name);


                    for (int k = 0; k < menuItems.menuItem.size(); k++) {
                        if (menuItems.menuItem.get(k).subCategoryId.equalsIgnoreCase(subCatg.SubCategories.get(j).id)) {

                            ListItemLinear.addView(mainCatg);
                            ListItemLinear.addView(subCatg2);


                            View mainItem = inflater.inflate(R.layout.rest_menu_item, ListItemLinear, false);
                            ListItemLinear.addView(mainItem);

                            TextView itemName = (TextView) mainItem.findViewById(R.id.title);
                            final TextView price = (TextView) mainItem.findViewById(R.id.title2);

                            TextView minus = (TextView) mainItem.findViewById(R.id.minus);
                            TextView plus = (TextView) mainItem.findViewById(R.id.plus);
                            final TextView value = (TextView) mainItem.findViewById(R.id.value);

                            itemName.setText(menuItems.menuItem.get(k).menuItem);
                            price.setText("Rs " + menuItems.menuItem.get(k).listPrice);

                            value.setText("" + counter);

                            final String iname = menuItems.menuItem.get(k).menuItem;
                            final int iprice = menuItems.menuItem.get(k).listPrice;


                            minus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (counter <= 0) {
                                        counter = 0;
                                        value.setText("" + counter);
                                    } else {
                                        counter -= 1;
                                        value.setText("" + counter);

                                        processSaveCurerntOrder(counter, iname, iprice);


                                    }


                                }
                            });

                            plus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (counter >= 9) {
                                        counter = 9;
                                        value.setText("" + counter);

                                    } else {
                                        counter += 1;
                                        value.setText("" + counter);
                                    }

                                    processSaveCurerntOrder(counter, iname, iprice);
                                }
                            });


                        }
                    }

                }
            }

          *//*  ListItemLinear.addView(subCatg);
            ListItemLinear.addView(mainItem);*//*
        }*/


        // resAdapter = new RestrauntMenuAdapter(RestrauntDetail.this, checkTypeList);
        //  listviewMenu.setAdapter(resAdapter);
    }

    private void fillCategory(Categories categories) {

        View viewCategory = getLayoutInflater().inflate(R.layout.rest_catg_item, null);
        ListItemLinear.addView(viewCategory, params);
        ListItemLinear.invalidate();

        TextView title = (TextView) viewCategory.findViewById(R.id.title);
        title.setText(categories.name);

        for (SubCategories subCategorie : subCatg.SubCategories) {
            if (subCategorie.categoryId.equalsIgnoreCase(categories.id)) {
                fillSubCategory(subCategorie);
            }

        }


    }

    private void fillSubCategory(SubCategories subCategorie) {

        View viewSubCategory = getLayoutInflater().inflate(R.layout.rest_subcatg_item, null);
        ListItemLinear.addView(viewSubCategory, params);
        ListItemLinear.invalidate();

        TextView title2 = (TextView) viewSubCategory.findViewById(R.id.subtitle);
        title2.setText(subCategorie.name);

        for (MenuItems menuItem : menuItems.menuItem) {

            if (menuItem.subCategoryId.equalsIgnoreCase(subCategorie.id)) {
                fillMenuItems(menuItem);
            }

        }

    }

    private void fillMenuItems(final MenuItems menuItem) {

        View viewMenu = getLayoutInflater().inflate(R.layout.rest_menu_item, null);
        ListItemLinear.addView(viewMenu, params);
        ListItemLinear.invalidate();

        TextView itemName = (TextView) viewMenu.findViewById(R.id.title);
        final TextView price = (TextView) viewMenu.findViewById(R.id.title2);

        TextView minus = (TextView) viewMenu.findViewById(R.id.minus);
        TextView plus = (TextView) viewMenu.findViewById(R.id.plus);
        final TextView value = (TextView) viewMenu.findViewById(R.id.value);
        itemName.setText(menuItem.menuItem);
        price.setText("Rs " + menuItem.listPrice);
        value.setText("" + counter);


        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int count = Integer.parseInt(value.getText().toString());
                count = count + 1;
                selectedItems.put(menuItem, count);
                value.setText(String.format("%d", count));
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int count = Integer.parseInt(value.getText().toString());
                if(count == 0){

                }else{
                    count = count - 1;

                    if(count == 0){
                        selectedItems.remove(menuItem);
                        value.setText(String.format("%d", count));
                    }else{
                        selectedItems.put(menuItem, count);
                        value.setText(String.format("%d",count));
                    }

                }

            }
        });


    }


    private void processSaveCurerntOrder(int counter, String ItemName, int Price) {

        SharedPreferences sp = getSharedPreferences("currentOrder", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("qty", counter);
        editor.putString("itemname", ItemName);
        editor.putInt("price", Price);

        editor.commit();

    }

    private void fillupdetails() {

        txtHotelName.setText(valuesRestraunt.Restraunt.get(listPosition).name.toUpperCase());
        tv_restaurant_mobile.setText("" + valuesRestraunt.Restraunt.get(listPosition).mobileNo);


        if (valuesRestraunt.Restraunt.get(listPosition).jainFoodAvailable) {
            menu.setText(" " + "JainFood");
        }


        if (valuesRestraunt.Restraunt.get(listPosition).northIndia) {
            menu.setText(" " + "North Indian");
        }


        if (valuesRestraunt.Restraunt.get(listPosition).southIndia) {
            menu.setText(" " + "South Indian");
        }

        if (valuesRestraunt.Restraunt.get(listPosition).pizza) {
            menu.setText(" " + "Pizza");
        }

        time.setText(valuesRestraunt.Restraunt.get(listPosition).morningOpeningTime + " - " + valuesRestraunt.Restraunt.get(listPosition).morningClosingTime + " Hrs , " + valuesRestraunt.Restraunt.get(listPosition).eveningOpeningTime + " - " + valuesRestraunt.Restraunt.get(listPosition).eveningClosingTime + " Hrs");
        minOrder.setText("Min. Order : " + valuesRestraunt.Restraunt.get(listPosition).minOrderAmount + " INR");

        if (valuesRestraunt.Restraunt.get(listPosition).deliveryCharges == 0)
            delivery.setText("Delivery : Free");
        else
            delivery.setText("Delivery : " + valuesRestraunt.Restraunt.get(listPosition).deliveryCharges + " INR");


        if (valuesRestraunt.Restraunt.get(listPosition).vegItems) {
            veg.setVisibility(View.VISIBLE);
            nonveg.setVisibility(View.GONE);
        } else {
            veg.setVisibility(View.GONE);
            nonveg.setVisibility(View.VISIBLE);
        }
    }


    void processFetchDetails(String link, final ProgressDialog pb, final int responseId) {


        pb.setMessage("Loading details...");
        pb.show();

        new GetPostClass(link, EnumType.GET) {
            @Override
            public void response(String response) {
                pb.dismiss();
                //Log.e("response "+responseId, response);

                try {

                    JSONObject mainObject = new JSONObject();
                    JSONArray subArray = new JSONArray();
                    JSONArray jsonArray = new JSONArray(response);

                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Log.e("resturnr list", jsonObject.toString());
                            subArray.put(i, jsonObject);
                        }


                        switch (responseId) {
                            case 1:
                                mainObject.put("Categories", subArray);
                                Log.e("response " + responseId, mainObject.toString());

                                catg = new GsonBuilder().create().fromJson(mainObject.toString(), TopCategories.class);
                                break;
                            case 2:
                                mainObject.put("SubCategories", subArray);
                                Log.e("response " + responseId, mainObject.toString());

                                subCatg = new GsonBuilder().create().fromJson(mainObject.toString(), TopSubCategories.class);
                                break;
                            case 3:
                                mainObject.put("MenuItems", subArray);
                                Log.e("response " + responseId, mainObject.toString());

                                menuItems = new GsonBuilder().create().fromJson(mainObject.toString(), TopMenuItems.class);
                                setAdapter();
                                break;
                        }


                    } else {
                        Log.e("no record found", "in " + responseId);
                    }


                } catch (Exception e) {
                    Log.e("exc", e.toString());
                }

            }

            @Override
            public void error(String error) {
                pb.dismiss();
                Toast.makeText(RestrauntDetail.this, error, Toast.LENGTH_SHORT).show();
            }
        }.call2();
    }
}

