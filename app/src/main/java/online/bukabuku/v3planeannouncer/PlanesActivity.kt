package online.bukabuku.v3planeannouncer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import online.bukabuku.v3planeannouncer.adapters.PlanesAdapter
import online.bukabuku.v3planeannouncer.databinding.FragmentPlanesBinding
import online.bukabuku.v3planeannouncer.network.dataclasses.PlanesRepository
import javax.inject.Inject

@AndroidEntryPoint
class PlanesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mCoroutineScope : CoroutineScope

    @Inject
    lateinit var repository: PlanesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_planes)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        mCoroutineScope = CoroutineScope(Dispatchers.IO)

        val planesAdapter = PlanesAdapter {}
        recyclerView.adapter = planesAdapter
        mCoroutineScope.launch {
            repository.allPlanes().collect() {
                planesAdapter.submitList(it)
            }
        }

    }

   /* override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlanesBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }*/

   /* override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val planesAdapter = PlanesAdapter({})
        recyclerView.adapter = planesAdapter
        // submitList() is a call that accesses the database. To prevent the
        // call from potentially locking the UI, you should use a
        // coroutine scope to launch the function. Using GlobalScope is not
        // best practice, and in the next step we'll see how to improve this.
        GlobalScope.launch(Dispatchers.IO) {
            //lifecycle.coroutineScope.launch {
                viewModel.allPlanes().collect() {
                    planesAdapter.submitList(it)
                }
            //}
        }

    }*/

}