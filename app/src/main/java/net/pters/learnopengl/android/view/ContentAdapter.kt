package net.pters.learnopengl.android.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.Section

class ContentAdapter(private val items: List<Section>) : BaseExpandableListAdapter() {

    override fun getGroupCount() = items.size

    override fun getChildrenCount(groupPosition: Int) = items[groupPosition].chapters.size

    override fun getGroup(groupPosition: Int) = items[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int) =
        items[groupPosition].chapters[childPosition]

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()

    override fun hasStableIds() = true

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val section = getGroup(groupPosition)
        val groupView = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.item_section, parent, false)
        (groupView as TextView).text = section.title
        return groupView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val chapter = getChild(groupPosition, childPosition)
        val groupView = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.item_chapter, parent, false)
        (groupView as TextView).text = chapter.title
        return groupView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true
}
