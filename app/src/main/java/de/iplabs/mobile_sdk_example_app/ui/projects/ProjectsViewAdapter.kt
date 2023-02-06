package de.iplabs.mobile_sdk_example_app.ui.projects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.iplabs.mobile_sdk.projectStorage.Project
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.databinding.ProjectBinding
import de.iplabs.mobile_sdk_example_app.fragments.ProjectsFragment

class ProjectsViewAdapter(
	private val fragment: ProjectsFragment
) : RecyclerView.Adapter<ProjectsViewAdapter.ProjectViewHolder>() {
	private lateinit var binding: ProjectBinding
	private val projectDataSet: MutableList<Project> = mutableListOf()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		binding = ProjectBinding.inflate(inflater, parent, false)

		return ProjectViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
		holder.bind(
			project = projectDataSet[holder.adapterPosition],
			fragment = fragment,
			popupMenuAnchor = holder.itemView.findViewById(R.id.project_popup_trigger)
		)
	}

	override fun getItemCount(): Int = projectDataSet.size

	fun setProjects(projects: List<Project>) {
		projectDataSet.clear()
		projectDataSet.addAll(projects)

		notifyDataSetChanged()
	}

	class ProjectViewHolder(
		private val binding: ProjectBinding
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(project: Project, fragment: ProjectsFragment, popupMenuAnchor: View) {
			binding.project = project
			binding.fragment = fragment
			binding.popupMenuAnchor = popupMenuAnchor

			binding.executePendingBindings()
		}
	}
}
