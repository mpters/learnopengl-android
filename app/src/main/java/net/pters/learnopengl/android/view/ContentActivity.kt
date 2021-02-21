package net.pters.learnopengl.android.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.pters.learnopengl.android.LearnOpenGL
import net.pters.learnopengl.android.databinding.ActivityContentBinding

class ContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        with(binding.explvContent) {
            val adapter = ContentAdapter(LearnOpenGL.content)
            setAdapter(adapter)
            setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
                startActivity(
                    SceneActivity.createIntent(
                        this@ContentActivity,
                        groupPosition,
                        childPosition
                    )
                )
                true
            }
        }
    }
}
