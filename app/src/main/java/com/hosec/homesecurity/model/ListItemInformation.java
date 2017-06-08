package com.hosec.homesecurity.model;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by D062572 on 29.05.2017.
 */

public abstract class ListItemInformation implements View.OnClickListener {

    public abstract String getTitle();
    public abstract String getSubtitle();
    public abstract int getImageId();

    public boolean animate(){
        return true;
    }

    public void customizeListItemTemplate(View listItemView){

    }
}
