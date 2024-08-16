package ua.com.radiokot.photoprism.features.gallery.folders.view

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import ua.com.radiokot.photoprism.R
import ua.com.radiokot.photoprism.base.view.BaseActivity
import ua.com.radiokot.photoprism.databinding.ActivityGalleryFoldersBinding
import ua.com.radiokot.photoprism.extension.autoDispose
import ua.com.radiokot.photoprism.extension.kLogger
import ua.com.radiokot.photoprism.features.gallery.folders.view.model.GalleryFolderListItem
import ua.com.radiokot.photoprism.features.gallery.folders.view.model.GalleryFoldersViewModel
import ua.com.radiokot.photoprism.features.gallery.search.extension.bindToViewModel
import ua.com.radiokot.photoprism.features.gallery.search.extension.fixCloseButtonColor
import ua.com.radiokot.photoprism.features.gallery.search.extension.hideUnderline

class GalleryFoldersActivity : BaseActivity() {

    private val log = kLogger("GalleryFoldersActivity")
    private lateinit var view: ActivityGalleryFoldersBinding
    private val viewModel: GalleryFoldersViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ActivityGalleryFoldersBinding.inflate(layoutInflater)
        setContentView(view.root)

        setSupportActionBar(view.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Init the list once it is laid out.
        view.foldersRecyclerView.doOnPreDraw {
            initList()
        }

        subscribeToEvents()

        // Allow the view model to intercept back press.
        onBackPressedDispatcher.addCallback(viewModel.backPressedCallback)
    }

    private fun initList() {
        val albumsAdapter = ItemAdapter<GalleryFolderListItem>()

        viewModel.itemsList.observe(this, albumsAdapter::setNewList)

        with(view.foldersRecyclerView) {
            // Safe dimensions of the list keeping from division by 0.
            // The fallback size is not supposed to be taken,
            // as it means initializing of a not laid out list.
            val listWidth = measuredWidth
                .takeIf { it > 0 }
                ?: FALLBACK_LIST_SIZE
                    .also {
                        log.warn { "initList(): used_fallback_width" }
                    }

            val minItemWidthPx =
                resources.getDimensionPixelSize(R.dimen.list_item_gallery_search_album_width)
            val spanCount = (listWidth / minItemWidthPx).coerceAtLeast(1)

            log.debug {
                "initList(): calculated_grid:" +
                        "\nspanCount=$spanCount," +
                        "\nrowWidth=$listWidth," +
                        "\nminItemWidthPx=$minItemWidthPx"
            }

            adapter = FastAdapter.with(albumsAdapter).apply {
                stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

                onClickListener = { _, _, item: GalleryFolderListItem, _ ->
                    // TODO handle click
                    true
                }
            }

            // Add the row spacing and make the items fill the column width
            // by overriding the layout manager layout params factory.
            layoutManager = object : GridLayoutManager(context, spanCount) {
                val rowSpacing: Int =
                    resources.getDimensionPixelSize(R.dimen.list_item_gallery_folder_margin_end)

                override fun generateLayoutParams(
                    c: Context,
                    attrs: AttributeSet
                ): RecyclerView.LayoutParams {
                    return super.generateLayoutParams(c, attrs).apply {
                        width = RecyclerView.LayoutParams.MATCH_PARENT
                        bottomMargin = rowSpacing
                    }
                }
            }

            FastScrollerBuilder(this)
                .useMd2Style()
                .setTrackDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.fast_scroll_track
                    )!!
                )
                .setThumbDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.fast_scroll_thumb
                    )!!
                )
                .build()
        }
    }

    private fun subscribeToEvents() = viewModel.events.subscribe { event ->
        log.debug {
            "subscribeToEvents(): received_new_event:" +
                    "\nevent=$event"
        }

        when (event) {
            is GalleryFoldersViewModel.Event.Finish ->
                finish()
        }

        log.debug {
            "subscribeToEvents(): handled_new_event:" +
                    "\nevent=$event"
        }
    }.autoDispose(this)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_overview, menu)

        // Set up the search.
        with(menu?.findItem(R.id.search_view)?.actionView as SearchView) {
            queryHint = getString(R.string.enter_the_query)
            fixCloseButtonColor()
            hideUnderline()
            bindToViewModel(viewModel, this@GalleryFoldersActivity)
        }

        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        private const val FALLBACK_LIST_SIZE = 100

    }
}
