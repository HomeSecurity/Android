package com.hosec.homesecurity.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.ListItemInformation;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GeneralListFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private TextView mTextView;
    private List<? extends ListItemInformation> mListItemInformation;

    public GeneralListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_rule_list, container, false);
        mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycleView);
        mTextView = (TextView) fragmentView.findViewById(R.id.tvEmpty);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);

        mRecyclerView.addItemDecoration(dividerItemDecoration);


        mRecyclerView.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);

        return fragmentView;
    }

    public <T extends ListItemInformation> void setData(List<T> listItemInformation){

        ListItemInformation[] ary = new ListItemInformation[listItemInformation.size()];
        listItemInformation.toArray(ary);
        mListItemInformation = listItemInformation;

        if(ary.length == 0){
            mRecyclerView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        }else{
            mRecyclerView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        }

        mRecyclerView.setAdapter(new ListItemAdapter(ary));
    }

    public List<? extends ListItemInformation> getData(){
        return mListItemInformation;
    }

}
