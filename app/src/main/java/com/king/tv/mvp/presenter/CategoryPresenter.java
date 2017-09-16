package com.king.tv.mvp.presenter;

import android.util.Log;

import com.king.base.util.LogUtils;
import com.king.tv.App;
import com.king.tv.bean.LiveCategory;
import com.king.tv.mvp.base.BasePresenter;
import com.king.tv.mvp.view.ICategoryView;
import com.king.tv.thread.ThreadPoolManager;


import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * @since 2017/2/21
 */

public class CategoryPresenter extends BasePresenter<ICategoryView> {

    final static String TAG = "hss CategoryPresenter";
    @Inject
    public CategoryPresenter(App app) {
        super(app);
    }

    public void getAllCategories(){//数据接收源
        Log.d(TAG,"getAllCategories");
        getView().showProgress();
        getAppComponent().getAPIService()//使用了Rxjava的特性
                .getAllCategories()//被观察者/数据提供者数据来自http://www.quanmin.tv/json/app/index/category/info-android.json?v=3.0.1&os=1&ver=4
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LiveCategory>>() {//观察者接收数据
                    @Override
                    public void onCompleted() {
                        if(isViewAttached())
                            getView().onCompleted();

                    }

                    @Override
                    public void onError(Throwable e) {
                        if(isViewAttached())
                            getView().onError(e);
                    }

                    @Override
                    public void onNext(final List<LiveCategory> list) {
                        LogUtils.d("Response:" + list);

                        ThreadPoolManager.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                getDaoSession().getLiveCategoryDao().insertOrReplaceInTx(list);
                            }
                        });
                        if(isViewAttached())
                            getView().onGetLiveCategory(list);

                    }
                });




    }

    public void getAllCategoriesByDB(){
        List<LiveCategory> list =  getDaoSession().getLiveCategoryDao().loadAll();
        LogUtils.d("list:" + list);
        if(list!=null && list.size()>0){
            if(isViewAttached())
                getView().onGetLiveCategory(list);
        }

    }


}
