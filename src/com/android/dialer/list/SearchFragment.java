/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.dialer.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.android.contacts.common.list.ContactEntryListAdapter;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.common.list.PhoneNumberPickerFragment;
import com.android.contacts.common.util.ViewUtil;
import com.android.dialer.DialtactsActivity;
import com.android.dialer.R;
import com.android.dialer.list.OnListFragmentScrolledListener;
import com.android.dialer.util.DialerUtils;

public class SearchFragment extends PhoneNumberPickerFragment {

    private OnListFragmentScrolledListener mActivityScrollListener;

    /*
     * Stores the untouched user-entered string that is used to populate the add to contacts
     * intent.
     */
    private String mAddToContactNumber;
    private int mActionBarHeight;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        setQuickContactEnabled(true);
        setDarkTheme(false);
        setPhotoPosition(ContactListItemView.getDefaultPhotoPosition(false /* opposite */));
        setUseCallableUri(true);

        try {
            mActivityScrollListener = (OnListFragmentScrolledListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnListFragmentScrolledListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isSearchMode()) {
            getAdapter().setHasHeader(0, false);
        }

        mActionBarHeight = ((DialtactsActivity) getActivity()).getActionBarHeight();

        final View parentView = getView();
        parentView.setPaddingRelative(
                parentView.getPaddingStart(),
                mActionBarHeight,
                parentView.getPaddingEnd(),
                parentView.getPaddingBottom());

        final ListView listView = getListView();

        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mActivityScrollListener.onListFragmentScrollStateChange(scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
            }
        });

        if (!getActivity().getActionBar().isShowing()) {
            parentView.setTranslationY(-mActionBarHeight);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtil.addBottomPaddingToListViewForFab(getListView(), getResources());
    }

    @Override
    protected void setSearchMode(boolean flag) {
        super.setSearchMode(flag);
        // This hides the "All contacts with phone numbers" header in the search fragment
        final ContactEntryListAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.setHasHeader(0, false);
        }
    }

    public void setAddToContactNumber(String addToContactNumber) {
        mAddToContactNumber = addToContactNumber;
    }

    @Override
    protected ContactEntryListAdapter createListAdapter() {
        DialerPhoneNumberListAdapter adapter = new DialerPhoneNumberListAdapter(getActivity());
        adapter.setDisplayPhotos(true);
        adapter.setUseCallableUri(super.usesCallableUri());
        return adapter;
    }

    @Override
    protected void onItemClick(int position, long id) {
        final DialerPhoneNumberListAdapter adapter = (DialerPhoneNumberListAdapter) getAdapter();
        final int shortcutType = adapter.getShortcutTypeFromPosition(position);

        if (shortcutType == DialerPhoneNumberListAdapter.SHORTCUT_INVALID) {
            super.onItemClick(position, id);
        } else if (shortcutType == DialerPhoneNumberListAdapter.SHORTCUT_DIRECT_CALL) {
            final OnPhoneNumberPickerActionListener listener =
                    getOnPhoneNumberPickerListener();
            if (listener != null) {
                listener.onCallNumberDirectly(getQueryString());
            }
        } else if (shortcutType == DialerPhoneNumberListAdapter.SHORTCUT_ADD_NUMBER_TO_CONTACTS) {
            final String number = TextUtils.isEmpty(mAddToContactNumber) ?
                    adapter.getFormattedQueryString() : mAddToContactNumber;
            final Intent intent = DialtactsActivity.getAddNumberToContactIntent(number);
            DialerUtils.startActivityWithErrorToast(getActivity(), intent,
                    R.string.add_contact_not_available);
        }
    }
}
