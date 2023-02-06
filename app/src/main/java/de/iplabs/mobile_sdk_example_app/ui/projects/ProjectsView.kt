package de.iplabs.mobile_sdk_example_app.ui.projects

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.iplabs.mobile_sdk.projectStorage.Project
import de.iplabs.mobile_sdk_example_app.fragments.ProjectsFragment

class ProjectsView {
	private lateinit var recyclerView: RecyclerView
	private lateinit var viewAdapter: ProjectsViewAdapter

	fun attach(context: Context, recyclerView: RecyclerView, fragment: ProjectsFragment) {
		val viewLayoutManager = LinearLayoutManager(context)
		this.viewAdapter = ProjectsViewAdapter(fragment = fragment)

		this.recyclerView = recyclerView.apply {
			layoutManager = viewLayoutManager
			adapter = viewAdapter
		}
	}

	fun setProjects(projects: List<Project>) {
		viewAdapter.setProjects(projects)
	}
}
