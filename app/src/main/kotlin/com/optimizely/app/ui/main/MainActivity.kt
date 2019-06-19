package com.optimizely.app.ui.main

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import com.optimizely.app.OptlyApplication
import com.optimizely.app.R
import com.optimizely.app.data.OptimizelyApisService
import com.optimizely.app.data.Project
import com.optimizely.app.data.Token
import com.optimizely.app.main.ProjectsDetailActivity
import com.optimizely.app.ui.BaseActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.onItemTouchListener
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers
import javax.inject.Inject

class MainActivity : BaseActivity(), ProjectsPresenter {
    @Inject lateinit var optimizelyApisService: OptimizelyApisService

    lateinit var view: ProjectsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OptlyApplication.graph.inject(this)

        val ui = MainActivityUi(this)
        ui.setContentView(this)
        view = ui

        load()

        // First load of this Activity.  This would be non-null if Activity is being recreated from
        // the user rotating the device or other configuration changes.
        if (savedInstanceState == null) {
            refresh()
        }
    }

    override fun refresh() {
        manageSub(refresh(Action1 {
            debug("Refreshed projects")
            //            view.onProjects(it)
        }, Action1 {
            error("Unable to refresh projects", it)
        }))
    }

    // Fetch projects from the server and store them
    fun refresh(success: Action1<List<Project>>, error: Action1<Throwable>): Subscription {
        return realm.where(Token::class.java).findFirstAsync().asObservable<Token>()
                .filter {
                    it.isLoaded
                }.first().map {
            realm.copyFromRealm(it)
        }.observeOn(Schedulers.io()).flatMap { authResponse ->
            optimizelyApisService.projects("Bearer ${authResponse.accessToken}")
        }.observeOn(AndroidSchedulers.mainThread()).flatMap { projects ->
            observable<List<Project>> { subscriber ->
                realm.executeTransactionAsync({
                    it.copyToRealmOrUpdate(projects)
                }, {
                    debug("Saved projects to realm")
                    subscriber.onNext(realm.where(Project::class.java).findAll())
                    subscriber.onCompleted()
                }, {
                    subscriber.onError(it)
                })
            }
        }.subscribe(success, error)
    }

    override fun load() {
        manageSub(load(Action1 {
            debug("Loaded projects")
            view.onProjects(it)
        }, Action1 {
            error("Unable to load projects", it)
        }))
    }

    fun load(success: Action1<List<Project>>, error: Action1<Throwable>): Subscription {
        return realm.where(Project::class.java).findAllAsync().asObservable()
                .filter { it.isLoaded }
                .map { it.toList() }
                .subscribe(success, error)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}

class MainActivityUi(val presenter: ProjectsPresenter? = null) : AnkoComponent<MainActivity>, AnkoLogger, ProjectsView {

    lateinit var projectsAdapter: ProjectsAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    class ProjectsAdapter(val ui: AnkoContext<MainActivity>, var projects: List<Project>) : RecyclerView.Adapter<ProjectsAdapter.ViewHolder>(), AnkoLogger {
        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            if (holder != null) {
                val project = projects[position]
                holder.textView.text = project.projectName
            }
            debug("Bound view holder")
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
            val textView = TextView(ui.ctx)
            textView.onClick { ui.ctx.startActivity<ProjectsDetailActivity>() }
            return ViewHolder(textView)
        }

        override fun getItemCount(): Int {
            return projects.size
        }

        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    }

    override fun onProjects(projects: List<Project>) {
        projectsAdapter.projects = projects
        projectsAdapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
    }

    override fun createView(ui: AnkoContext<MainActivity>) = ui.apply {
        projectsAdapter = ProjectsAdapter(ui, emptyList())
        verticalLayout {
            swipeRefreshLayout = swipeRefreshLayout {
                onRefresh {
                    presenter?.refresh()
                }
                recyclerView {
                    layoutManager = LinearLayoutManager(ctx)
                    this.adapter = projectsAdapter
                }
            }
        }
    }.view
}

interface ProjectsPresenter {
    fun refresh()
    fun load()
}

interface ProjectsView {
    fun onProjects(projects: List<Project>)
}
