package com.hosec.homesecurity.model;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.activities.HomeActivity;
import com.hosec.homesecurity.activities.RuleDetailActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D062572 on 07.06.2017.
 */

public class RuleItemInformation extends ListItemInformation {

    private Rule mRule;

    public RuleItemInformation(Rule rule) {
        super();
        mRule = rule;
    }

    @Override
    public String getTitle() {
        return mRule.getName();
    }

    @Override
    public String getSubtitle() {
        return mRule.active() ? "Active" : "Inactive";
    }

    @Override
    public int getImageId() {
        return R.mipmap.ic_rule;
    }

    @Override
    public void onClick(View v) {
        Activity activity = (Activity) v.getContext();
        Intent intent = new Intent(activity, RuleDetailActivity.class);
        intent.putExtra(RuleDetailActivity.RULE_KEY, mRule);
        activity.startActivityForResult(intent, HomeActivity.REQUEST_CODE_DETAIL);
    }

    public static List<RuleItemInformation> createRuleItemInformation(List<Rule> rules) {
        List<RuleItemInformation> newList = new ArrayList<>(rules.size());
        for (Rule r : rules) {
            newList.add(new RuleItemInformation(r));
        }
        return newList;
    }
}
