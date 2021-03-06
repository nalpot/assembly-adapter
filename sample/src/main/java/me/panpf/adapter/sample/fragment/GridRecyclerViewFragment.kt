package me.panpf.adapter.sample.fragment

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.ApplicationInfo
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.panpf.adapter.AssemblyRecyclerAdapter
import me.panpf.adapter.recycler.AssemblyGridLayoutManager
import me.panpf.adapter.sample.R
import me.panpf.adapter.sample.bean.AppInfo
import me.panpf.adapter.sample.bindView
import me.panpf.adapter.sample.itemfactory.AppItem
import me.panpf.adapter.sample.itemfactory.AppListHeaderItem
import java.io.File
import java.util.*

class GridRecyclerViewFragment : Fragment() {

    private val recyclerView: RecyclerView by bindView(R.id.list_recyclerViewFragment_content)
    private val appsViewModel: AppsViewModel by lazy { ViewModelProviders.of(this).get(AppsViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        recyclerView.layoutManager = AssemblyGridLayoutManager(context, 3, recyclerView)

        val adapter = AssemblyRecyclerAdapter().apply {
            addItemFactory(AppItem.Factory())
            addItemFactory(AppListHeaderItem.Factory().fullSpan(recyclerView))
        }
        recyclerView.adapter = adapter

        appsViewModel.apps.observe(this, android.arch.lifecycle.Observer {
            it ?: return@Observer

            val systemAppList = it[0]
            val userAppList = it[1]

            val systemAppListSize = systemAppList.size
            val userAppListSize = userAppList.size

            var dataListSize = if (systemAppListSize > 0) systemAppListSize + 1 else 0
            dataListSize += if (userAppListSize > 0) userAppListSize + 1 else 0

            val dataList = ArrayList<Any>(dataListSize)
            if (userAppListSize > 0) {
                dataList.add(String.format("自安装应用%d个", userAppListSize))
                dataList.addAll(userAppList)
            }
            if (systemAppListSize > 0) {
                dataList.add(String.format("系统应用%d个", systemAppListSize))
                dataList.addAll(systemAppList)
            }

            adapter.dataList = dataList
            recyclerView.scheduleLayoutAnimation()
        })

        appsViewModel.load()
    }
}

class AppsViewModel(application: Application) : AndroidViewModel(application) {
    val apps = MutableLiveData<Array<List<AppInfo>>>()

    fun load() {
        LoadDataTask(apps, getApplication()).execute()
    }
}

class LoadDataTask(private val apps: MutableLiveData<Array<List<AppInfo>>>, private val appContext: Application) : AsyncTask<Int, Int, Array<List<AppInfo>>>() {
    override fun doInBackground(vararg params: Int?): Array<List<AppInfo>>? {
        val packageManager = appContext.packageManager
        val packageInfoList = packageManager.getInstalledPackages(0)
        val systemAppList = ArrayList<AppInfo>()
        val userAppList = ArrayList<AppInfo>()
        for (packageInfo in packageInfoList) {
            val appInfo = AppInfo(true)
            appInfo.packageName = packageInfo.packageName
            appInfo.name = packageInfo.applicationInfo.loadLabel(packageManager).toString()
            appInfo.sortName = appInfo.name
            appInfo.id = packageInfo.packageName
            appInfo.versionName = packageInfo.versionName
            appInfo.apkFilePath = packageInfo.applicationInfo.publicSourceDir
            appInfo.appSize = Formatter.formatFileSize(appContext, File(appInfo.apkFilePath).length())
            appInfo.versionCode = packageInfo.versionCode
            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                systemAppList.add(appInfo)
            } else {
                userAppList.add(appInfo)
            }
        }

        systemAppList.sortWith(Comparator { lhs, rhs ->
            (lhs.sortName ?: "").compareTo(rhs.sortName ?: "")
        })

        return arrayOf(systemAppList, userAppList)
    }

    override fun onPostExecute(appInfoLists: Array<List<AppInfo>>) {
        apps.value = appInfoLists
    }
}